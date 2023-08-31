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
 *  MutationController.java
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.paramservice.gql;

import com.ailegorreta.commons.utils.HasLogger;
import com.ailegorreta.paramservice.domain.*;
import com.ailegorreta.paramservice.gql.types.*;
import com.ailegorreta.paramservice.service.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

/**
 * note : spring boot starter created an `AnnotatedDataFetchersConfigurer` to register data fetchers from
 * `@GraphQlController` clazz
 *
 * @project param-server-repo
 * @autho rlh
 * @date August 2023
 */
@Controller
@Validated
@RequiredArgsConstructor
public class MutationController implements HasLogger {

    private final SystemDateService systemDateService;
    private final SystemRateService systemRateService;
    private final DocumentTypeService documentTypeService;
    private final TemplateService templateService;

    @MutationMapping
    public SystemDate addSystemDate(@Argument("systemDateInput") SystemDateInput systemDateInput) {
        return systemDateService.addSystemDate(systemDateInput);
    }

    @MutationMapping
    public SystemDate updateSystemDate(@Argument("systemDateInput")SystemDateInput systemDateInput)  {
        return systemDateService.updateSystemDate(systemDateInput);
    }

    @MutationMapping
    public SystemDate deleteSystemDate(@Argument("id") UUID id,
                                       @Argument("user")String user) {
        return systemDateService.deleteSystemDate(id, user);
    }

    @MutationMapping
    public SystemRate addSystemRate(@Argument("systemRateInput")SystemRateInput systemRateInput) {
        return systemRateService.addSystemRate(systemRateInput);
    }

    @MutationMapping
    public SystemRate updateSystemRate(@Argument("systemRateInput")SystemRateInput systemRateInput)  {
        return systemRateService.updateSystemRate(systemRateInput);
    }

    @MutationMapping
    public SystemRate deleteSystemRate(@Argument("id")UUID id,
                                       @Argument("user")String user) {
        return systemRateService.deleteSystemRate(id, user);
    }

    @MutationMapping
    public DocumentType addDocumentType(@Argument("documentTypeInput") DocumentTypeInput documentTypeInput) {
        return documentTypeService.addDocumentType(documentTypeInput);
    }

    @MutationMapping
    public DocumentType updateDocumentType(@Argument("documentTypeInput")DocumentTypeInput documentTypeInput)  {
        return documentTypeService.updateDocumentType(documentTypeInput);
    }

    @MutationMapping
    public DocumentType deleteDocumentType(@Argument("id")UUID id,
                                           @Argument("user")String user) {
        return documentTypeService.deleteDocumentType(id, user);
    }


    @MutationMapping
    public Template addTemplate(@Argument("templateInput") TemplateInput templateInput) {
        return templateService.addTemplate(templateInput);
    }

    @MutationMapping
    public Template updateTemplate(@Argument("templateInput")TemplateInput templateInput)  {
        return templateService.updateTemplate(templateInput);
    }

    @MutationMapping
    public TemplateField addTemplateField(@Argument("id")UUID id,
                                          @Argument("templateFieldInput")TemplateFieldInput templateFieldInput)  {
        return templateService.addTemplateField(id, templateFieldInput);
    }

    @MutationMapping
    public TemplateField updateTemplateField(@Argument("id")UUID id,
                                             @Argument("templateFieldInput")TemplateFieldInput templateFieldInput)  {
        return templateService.updateTemplateField(id, templateFieldInput);
    }

    @MutationMapping
    public TemplateField deleteTemplateField(@Argument("id")UUID id,
                                             @Argument("fieldId")UUID fieldId,
                                             @Argument("user")String user)  {
        return templateService.deleteTemplateField(id, fieldId, user);
    }

    @MutationMapping
    public Template deleteTemplate(@Argument("id")UUID id,
                                   @Argument("user")String user) {
        return templateService.deleteTemplate(id, user);
    }

    @MutationMapping
    public Datasource addDatasource(@Argument("datasourceInput") DatasourceInput datasourceInput) {
        return templateService.addDatasource(datasourceInput);
    }

    @MutationMapping
    public Datasource updateDatasource(@Argument("datasourceInput")DatasourceInput datasourceInput)  {
        return templateService.updateDatasource(datasourceInput);
    }

    @MutationMapping
    public DatasourceField addDatasourceField(@Argument("id")UUID id,
                                              @Argument("datasourceFieldInput")DatasourceFieldInput datasourceFieldInput)  {
        return templateService.addDatasourceField(id, datasourceFieldInput);
    }

    @MutationMapping
    public DatasourceField updateDatasourceField(@Argument("id")UUID id,
                                                 @Argument("datasourceFieldInput")DatasourceFieldInput datasourceFieldInput)  {
        return templateService.updateDatasourceField(id, datasourceFieldInput);
    }

    @MutationMapping
    public DatasourceField deleteDatasourceField(@Argument("id")UUID id,
                                                 @Argument("fieldId")UUID fieldId,
                                                 @Argument("user")String user)  {
        return templateService.deleteDatasourceField(id, fieldId, user);
    }

    @MutationMapping
    public Datasource deleteDatasource(@Argument("id")UUID id,
                                       @Argument("user")String user) {
        return templateService.deleteDatasource(id, user);
    }

    @Override
    public Logger getLogger() { return HasLogger.DefaultImpls.getLogger(this); }

}
