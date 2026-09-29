package com.example.demo.controladores;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.example.demo.modelo.Conserje;
import com.example.demo.repositorios.ConserjeRepositorio;

@SpringBootTest
@Transactional
class ConciergeCountTest {

    @Autowired
    private WebApplicationContext applicationContext;

    @Autowired
    private ConserjeRepositorio conciergeRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
        conciergeRepository.deleteAllInBatch();
    }

    @Test
    void returnsZeroWhenNoConciergesExist() throws Exception {
        mockMvc.perform(get("/api/conserjes/stats/count"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void returnsNumberOfRegisteredConcierges() throws Exception {
        conciergeRepository.saveAllAndFlush(List.of(
                new Conserje("Alice", "test-pass-135"),
                new Conserje("Bob", "test-pass-246")));

        mockMvc.perform(get("/api/conserjes/stats/count"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.total").value(2));
    }
}
