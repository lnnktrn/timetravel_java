package com.lnnktrn.timetravel_java.controller;

import com.lnnktrn.timetravel_java.controller.v2.RecordController;
import com.lnnktrn.timetravel_java.service.RecordService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecordController.class)
class RecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecordService recordService;

    @Test
    void get_returns404_whenLatestRecordDoesNotExist() throws Exception {
        when(recordService.getLatestRecord(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v2/records/1/latest"))
                .andExpect(status().isNotFound());
    }
}