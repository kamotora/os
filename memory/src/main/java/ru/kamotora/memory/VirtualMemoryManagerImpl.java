package ru.kamotora.memory;

import lombok.experimental.SuperBuilder;
import ru.kamotora.RandomUtil;
import ru.kamotora.memory.printer.PictureMemoryPrinter;
import ru.kamotora.output.RichConsole;
import ru.kamotora.output.RichTextConfig;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuperBuilder
public class VirtualMemoryManagerImpl extends VirtualMemoryManager {
    int processesCount;
    int timer;

    @Override
    public void accept(List<Process> processes) {
        List<PhysicalPage> memoryPages = initSystemMemoryPages();
        List<ProcessTable> processTables = new ArrayList<>();
        // lifecycle
        while (timer++ < lifetime()) {
            tryAddNewProcess(memoryPages, processTables, processes);

            // print memory
            RichConsole.print(RichTextConfig.metaMessageStyle(), "Time tick: %s", timer);
            new PictureMemoryPrinter().print(memoryPages);
            if (!processes.isEmpty()) {
                executeRandomProcess(processes, memoryPages, processTables);
            }

            // Вывод
            for (int i = 0; i < processTables.size(); i++) {
                ProcessTable table = processTables.get(i);
                PrettyTable prettyTable = new PrettyTable("virtualAddress", "isReadOnly", "isChanged", "wasSwapped", "physicalAddress", "lastAllocationTime");
                RichConsole.print(RichTextConfig.metaMessageStyle(),
                        "\nProcess: %s", table.label());
                RichConsole.print(RichTextConfig.metaMessageStyle(),
                        "Time remain: %s", processes.get(i).lifetime());
                for (int j = 0; j < table.pagesInfo().size(); j++) {
                    PageInfo info = table.pagesInfo().get(j);
                    var physicalPage = memoryPages.stream()
                            .filter(page -> info.physicalAddress() == page.address())
                            .findFirst()
                            .orElse(null);
                    var lastAllocationTime = Optional.ofNullable(physicalPage)
                            .map(PhysicalPage::lastAllocationTime)
                            .orElse(-1);
                    prettyTable.addRow(info.virtualAddress(), info.isReadOnly(), info.isChanged(), info.wasSwapped(), info.physicalAddress(), lastAllocationTime);
                }
                System.out.println(prettyTable);
            }
        }
    }

    private void executeRandomProcess(List<Process> processes, List<PhysicalPage> memoryPages, List<ProcessTable> processTables) {
        Process activeProcess = RandomUtil.choiceElement(processes);
        RichConsole.print(RichTextConfig.metaMessageStyle(),
                "Active process: %s", activeProcess.label());
        // Выбираем активную страницу процесса
        var activePage = RandomUtil.choiceElement(activeProcess.virtualPages());
        int addressOfActivePage = activePage.address();
        RichConsole.print(RichTextConfig.metaMessageStyle(),
                "Active virtual page: %s", addressOfActivePage);
        processTables.stream()
                .filter(processTable -> processTable.label().equals(activeProcess.label()))
                .findFirst()
                .ifPresentOrElse(processTable -> {
                    long allocatedPagesCount = processTable.pagesInfo().stream().filter(PageInfo::isAllocated).count();
                    RichConsole.print(RichTextConfig.metaMessageStyle(),
                            "Virtual pages for this process: %s, allocated: %s",
                            activeProcess.virtualPages().size(), allocatedPagesCount);
                    PageInfo activePageInfo = processTable.pagesInfo().get(addressOfActivePage);
                    if (!activePageInfo.isAllocated()) {
                        RichConsole.print(RichTextConfig.metaMessageStyle(),
                                "Active page isn't loaded, allocate memory for it");
                        addPage(memoryPages, activePageInfo, processTables);
                    }
                    // Если страница доступна для чтения, пишем в неё с некоторой вероятностью
                    if (!activePageInfo.isReadOnly() && RandomUtil.roll()) {
                        activePageInfo.isChanged(true);
                        activePageInfo.wasSwapped(false);
                    }
                }, () -> {
                    System.err.println(processTables);
                    throw new RuntimeException("Not found process " + activeProcess.label() + " in process table");
                });
        activeProcess.lifetime(activeProcess.lifetime() - 1);
        // Время процесса вышло - освобождаем память
        if (activeProcess.lifetime() < 0) {
            RichConsole.print(RichTextConfig.metaMessageStyle(),
                    "Stop active process, lifetime is ended...");
            for (int i = 0; i < processTables.size(); i++) {
                ProcessTable processTable = processTables.get(i);
                if (processTable.label().equals(activeProcess.label())) {
                    for (PageInfo pageInfo : processTable.pagesInfo()) {
                        for (PhysicalPage memoryPage : memoryPages) {
                            if (pageInfo.physicalAddress() == memoryPage.address()) {
                                memoryPage.isUsed(false);
                            }
                        }
                    }
                    processTables.remove(processTable);
                }
            }
            processes.remove(activeProcess);
        }
    }

    private void tryAddNewProcess(List<PhysicalPage> memory, List<ProcessTable> processTables, List<Process> processes) {
        if (processes.size() < processesLimit() && RandomUtil.roll(processCreationProbability())) {
            int processLifetime = randomLifetime();
            int pagesCount = randomPagesCount();
            var processVirtualPages = IntStream.range(0, pagesCount)
                    .mapToObj(virtualAddress -> Process.VirtualPage.builder()
                            .address(virtualAddress)
                            .isReadOnly(RandomUtil.roll(readOnlyPageProbability()))
                            .isChanged(false)
                            .build())
                    .collect(Collectors.toList());
            var newProcess = Process.builder()
                    .label(String.format("%s; pid: %s", RandomUtil.choiceElement(labels()), processesCount))
                    .lifetime(processLifetime)
                    .virtualPages(processVirtualPages)
                    .build();
            processes.add(newProcess);
            processesCount++;
            addProcess(newProcess, memory, processTables);
        }
    }

    /**
     * Добавить виртуальные адреса процесса в таблицу
     */
    private void addProcess(Process process, List<PhysicalPage> physicalMemoryPages, List<ProcessTable> processTables) {
        var pt = ProcessTable.builder()
                .label(process.label())
                .build();
        List<PageInfo> processPagesInfo = process.virtualPages().stream()
                .map(virtualPageOfProcess -> PageInfo.builder()
                        .wasSwapped(false)
                        .physicalAddress(-1)
                        .isReadOnly(virtualPageOfProcess.isReadOnly())
                        .virtualAddress(virtualPageOfProcess.address())
                        .owner(pt)
                        .build())
                .peek(pageInfo -> tryAddPage(physicalMemoryPages, pageInfo, processTables))
                .collect(Collectors.toList());

        pt.pagesInfo(processPagesInfo);
        processTables.add(pt);
    }

    /**
     * Выделяем процессу память с вероятностью addingProbability
     */
    private void tryAddPage(List<PhysicalPage> physicalMemoryPages, PageInfo pageInfo, List<ProcessTable> processTables) {
        var usedPagesCount = physicalMemoryPages.stream()
                .filter(PhysicalPage::isUsed)
                .count();
        double percentageMemoryUsed = usedPagesCount / (double) physicalMemoryPages.size();
        double addingProbability = percentageMemoryUsed > memoryOccupiedThreshold() ?
                1 - percentageMemoryUsed : 1d;
        if (RandomUtil.roll(addingProbability)) {
            addPage(physicalMemoryPages, pageInfo, processTables);
        }
    }

    /**
     * Выделяем процессу память
     */
    private void addPage(List<PhysicalPage> physicalMemoryPages, PageInfo pageInfo, List<ProcessTable> processTables) {
        PhysicalPage freePhysicalPage = null;
        while (Objects.isNull(freePhysicalPage)) {
            // Ищем свободную страницу
            freePhysicalPage = physicalMemoryPages.stream()
                    .filter(page -> !page.isUsed())
                    .findFirst()
                    .orElse(null);
            if (Objects.nonNull(freePhysicalPage)) {
                // Закрепляем страницу за процессом
                freePhysicalPage.isUsed(true);
                freePhysicalPage.lastAllocationTime(timer);
                pageInfo.physicalAddress(freePhysicalPage.address());
            } else {
                // Если свободной нет, пробуем освободить (имитация своппинга)
                freeMemory(physicalMemoryPages, processTables);
            }
        }
    }

    private void freeMemory(List<PhysicalPage> memoryPages, List<ProcessTable> processTables) {
        RichConsole.print(RichTextConfig.metaMessageStyle(), "Not found free memory, start swapping...");
        memoryPages.stream()
                // Ищем страницу, которая не выгружалась дольше всего (FIFO)
                .min(Comparator.comparing(PhysicalPage::lastAllocationTime))
                .ifPresent(page -> {
                    // Помещаем страницу как свободную
                    page.lastAllocationTime(-1);
                    page.isUsed(false);
                    RichConsole.print(RichTextConfig.metaMessageStyle(),
                            "Release occupied memory with physical address %s", page.address());
                    // Ищем эту страницу в таблице страниц
                    processTables.stream()
                            .flatMap(processTable -> processTable.pagesInfo().stream())
                            .filter(info -> info.physicalAddress() == page.address())
                            .findFirst().ifPresentOrElse(info -> {
                                RichConsole.print(RichTextConfig.metaMessageStyle(),
                                        "Start swapping page (process: %s, virtual addr: %s, physical  addr: %s)",
                                        info.owner().label(), info.virtualAddress(), info.physicalAddress());
                                info.physicalAddress(-1);
                                if (info.isChanged()) {
                                    RichConsole.print(RichTextConfig.metaMessageStyle(),
                                            "Page is was changed. Swap it", page.address());
                                    info.wasSwapped(true);
                                } else if (!info.wasSwapped()) {
                                    RichConsole.print(RichTextConfig.metaMessageStyle(),
                                            "Page is was NOT changed, but not found in swap file. Swap it", page.address());
                                    info.wasSwapped(true);
                                } else {
                                    RichConsole.print(RichTextConfig.metaMessageStyle(),
                                            "Page not changed and already contains in swap file", page.address());
                                }
                            }, () -> {
                                System.err.println(processTables);
                                throw new RuntimeException("Not found physical page " + page.address() + " in process table");
                            });
                });
    }

    private List<PhysicalPage> initSystemMemoryPages() {
        return IntStream.range(0, pagesCount())
                .mapToObj(i -> PhysicalPage.builder()
                        .address(i)
                        .isUsed(false)
                        .lastAllocationTime(-1)
                        .build())
                .toList();
    }
}
