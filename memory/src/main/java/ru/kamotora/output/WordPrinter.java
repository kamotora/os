package ru.kamotora.output;

import lombok.SneakyThrows;
import org.apache.poi.xwpf.usermodel.*;
import ru.kamotora.memory.PhysicalPage;
import ru.kamotora.memory.PrettyTable;

import java.io.FileOutputStream;
import java.util.List;

public class WordPrinter extends AbstractPrinter {

    private static final AbstractPrinter log = new ConsolePrinter();
    //Blank Document
    private static final XWPFDocument document = new XWPFDocument();

    public static final String DEFAULT_FILE_NAME = "output.docx";

    @Override
    public void newLine() {
        document.createParagraph().createRun().addBreak();
        log.newLine();
    }

    @Override
    public void print(String message, RichTextConfig config) {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = defaultRun(paragraph);
        if (config != null) {
            if (config.getColor() != null) {
                run.setColor(config.getColor().getRgbAsHex());
            }
            if (config.getBackground() != null) {
                run.setTextHighlightColor(config.getBackground().name().toLowerCase());
            }
        }
        run.setText(message);

        log.print(message, config);
    }

    @Override
    public void print(List<PhysicalPage> virtualMemory) {
        //create table
        XWPFTable table = document.createTable(1, virtualMemory.size());
        XWPFTableRow row = table.getRow(0);
        for (int i = 0; i < virtualMemory.size(); i++) {
            PhysicalPage memoryPage = virtualMemory.get(i);
            var cell = row.getCell(i);
            cell.removeParagraph(0);
            var paragraph = cell.addParagraph();
            var run = defaultRun(paragraph);
            Background backgroundForPage = getBackgroundForPage(memoryPage);
            run.setText(String.valueOf(memoryPage.address()));
            cell.getCTTc().addNewTcPr().addNewShd().setFill(backgroundForPage.getRgbAsHex());

        }
        log.print(virtualMemory);
    }

    @Override
    public void print(PrettyTable prettyTable) {
        XWPFTable table = document.createTable(prettyTable.getData().size() + 1, prettyTable.getHeaders().size());
        XWPFTableRow titleRow = table.getRow(0);
        for (int i = 0; i < prettyTable.getHeaders().size(); i++) {
            var header = prettyTable.getHeaders().get(i);
            var cell = titleRow.getCell(i);
            cell.removeParagraph(0);
            var paragraph = cell.addParagraph();
            var run = defaultRun(paragraph);
            run.setText(header);
        }
        for (int i = 0; i < prettyTable.getData().size(); i++) {
            var dataList = prettyTable.getData().get(i);
            XWPFTableRow row = table.getRow(i + 1);
            for (int j = 0; j < dataList.size(); j++) {
                var dataText = dataList.get(j);
                var cell = row.getCell(j);

                cell.removeParagraph(0);
                var paragraph = cell.addParagraph();
                var run = defaultRun(paragraph);
                run.setText(dataText);
            }
        }
        table.setWidthType(TableWidthType.PCT);
        log.print(prettyTable);
    }

    @Override
    @SneakyThrows
    public void save() {
        //Write the Document in file system
        FileOutputStream out = new FileOutputStream(DEFAULT_FILE_NAME, false);
        document.write(out);
        out.close();
        log.print("logs saved into %s", DEFAULT_FILE_NAME);
    }

    private static XWPFRun defaultRun(XWPFParagraph paragraph) {
        XWPFRun run = paragraph.createRun();
        run.setFontSize(10);
        run.setFontFamily("Times New Roman");
        run.removeBreak();
        return run;
    }
}
