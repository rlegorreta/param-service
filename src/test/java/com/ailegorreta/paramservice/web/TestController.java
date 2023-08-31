/* Copyright (c) 2023, LegoSoft Soluciones, S.C.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are not permitted.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 *  TestController.java
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.paramservice.web;

import com.ailegorreta.paramservice.domain.SystemRate;
import com.ailegorreta.paramservice.domain.SystemRateRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * This is a dummy controller for testing Webmvc purpose only, i.e., all REST calls for param-service are done using
 * GraphQL schema or Spring Data REST and therefore use GraphqlTester class or for Spring Data REST use on of the
 * following classes: DatasourceRestRepositoryTests, TemplateRestRepositoryTests and RelationshipsRestTests.
 *
 * @project: param-service
 * @author: rlh
 * @date: August 2023
 */
@Controller
@RequestMapping("param/tests")
public class TestController {
    private final SystemRateRepository systemRateRepository;

    public TestController(SystemRateRepository systemRateRepository) {
        this.systemRateRepository = systemRateRepository;
    }

    @PostMapping("all")
    List<SystemRate> getAllSystemRate() {
        return systemRateRepository.findAll();
    }

    @PostMapping("name")
    SystemRate getSystemRateByName(@Valid @RequestBody SystemRate systemRate) {
        return systemRateRepository.findSystemRateByName(systemRate.getName());
    }

}
