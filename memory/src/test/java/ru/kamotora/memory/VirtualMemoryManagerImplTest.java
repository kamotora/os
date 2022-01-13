package ru.kamotora.memory;

import org.junit.jupiter.api.Test;

class VirtualMemoryManagerImplTest {
    @Test
    public void manageTest() {
        VirtualMemoryManager vmm = VirtualMemoryManagerImpl.builder()
                .build();

        vmm.manage();
    }

}