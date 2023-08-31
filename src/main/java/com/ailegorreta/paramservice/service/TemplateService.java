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
 *  TemplateService.java
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.paramservice.service;

import com.ailegorreta.commons.utils.HasLogger;
import com.ailegorreta.paramservice.domain.*;
import com.ailegorreta.paramservice.gql.types.DatasourceFieldInput;
import com.ailegorreta.paramservice.gql.types.DatasourceInput;
import com.ailegorreta.paramservice.gql.types.TemplateFieldInput;
import com.ailegorreta.paramservice.gql.types.TemplateInput;
import com.ailegorreta.resourceserver.utils.UserContext;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Mutation service for all Templates operations: Templates, Datasource and Mapping
 *
 * @project param-service
 * @author rlh
 * @date August 2023
 */
@Service
@RequiredArgsConstructor
public class TemplateService implements HasLogger {

    final TemplateRepository templateRepository;
    final TemplateFieldRepository templateFieldRepository;
    final DatasourceRepository datasourceRepository;
    final DatasourceFieldRepository dataSourceFieldRepository;
    final EventService          eventService;

    public Template addTemplate(TemplateInput templateInput) {
        var template = Template.fromDTO(templateInput);

        eventService.sendEvent(UserContext.getCorrelationId(), templateInput.getUserModify(),
                "ANADE_NUEVO_TEMPLATE", templateInput);
        getLogger().info("Add a new template " + templateInput.getNombre());

        return templateRepository.save(template);
    }

    public Template updateTemplate(TemplateInput templateInput)  {
        // Validate that the template already exists
        var template = templateRepository.findById(templateInput.getId());

        Validate.isTrue(template.isPresent(), "El template ya NO existe en el catálogo.");

        template.get().setNombre(templateInput.getNombre());
        template.get().setFileRepo(templateInput.getFileRepo());
        template.get().setDestino(Template.DestinoType.valueOf(templateInput.getDestino()));
        template.get().setJson(templateInput.getJson());
        template.get().setBlockly(templateInput.getBlockly());
        template.get().setActivo(templateInput.getActivo());
        template.get().setFechaModificacion(LocalDate.now());

        eventService.sendEvent(UserContext.getCorrelationId(), templateInput.getUserModify(),
                "MODIFICA_TEMPLATE", template.get());
        getLogger().info("Update an existing template " + template.get().getNombre());

        return templateRepository.save(template.get());
    }

    public TemplateField addTemplateField(UUID idTemplate, TemplateFieldInput templateFieldInput)  {
        // Validate that the datasource already exists
        var template = templateRepository.findById(idTemplate);

        Validate.isTrue(template.isPresent(), "El template ya NO existe en el catálogo.");

        template.get().setFechaModificacion(LocalDate.now());

        var templateField = TemplateField.fromDTO(templateFieldInput, template.get());

        template.get().getCampos().add(templateField);
        templateField.setTemplate(template.get());

        templateFieldRepository.save(templateField);
        templateRepository.save(template.get());

        eventService.sendEvent(UserContext.getCorrelationId(), templateFieldInput.getUserModify(),
                "ANADE_TEMPLATE_FIELD", template.get());
        getLogger().info("Add a field " + templateFieldInput.getNombre() + " to the template " + template.get().getNombre());

        return templateField;
    }

    public TemplateField updateTemplateField(UUID idTemplate, TemplateFieldInput templateFieldInput)  {
        // Validate that the datasource already exists
        var template = templateRepository.findById(idTemplate);

        Validate.isTrue(template.isPresent(), "El template ya NO existe en el catálogo.");

        TemplateField templateField = null;

        for (TemplateField ds : template.get().getCampos())
            if (ds.getId().equals(templateFieldInput.getId())) templateField = ds;

        Validate.isTrue(templateField != null, "El campo del template NO existe en le catálogo");

        template.get().setFechaModificacion(LocalDate.now());
        templateField.setNombre(templateFieldInput.getNombre());
        templateField.setTipo(TemplateField.FieldType.valueOf(templateFieldInput.getTipo()));
        templateField.setValorDefault(templateFieldInput.getValorDefault());

        eventService.sendEvent(UserContext.getCorrelationId(), templateFieldInput.getUserModify(),
                "ACTUALIZA_TEMPLATE_FIELD", template.get());
        getLogger().info("Update a field " + templateFieldInput.getNombre() + " to the template " + template.get().getNombre());

        templateRepository.save(template.get());

        return templateField;
    }

    public TemplateField deleteTemplateField(UUID idTemplate, UUID fieldId, String user)  {
        // Validate that the datasource already exists
        var template = templateRepository.findById(idTemplate);

        Validate.isTrue(template.isPresent(), "El template ya NO existe en el catálogo.");

        TemplateField templateField = null;

        for (TemplateField ds : template.get().getCampos())
            if (ds.getId().equals(fieldId)) templateField = ds;

        Validate.isTrue(templateField != null, "El campo del template NO existe en le catálogo");

        template.get().setFechaModificacion(LocalDate.now());
        template.get().getCampos().remove(templateField);

        eventService.sendEvent(UserContext.getCorrelationId(), user,
                "ELIMINA_TEMPLATE_FIELD", template.get());
        getLogger().info("Delete a field " + templateField.getNombre() + " to the template " + template.get().getNombre());

        templateRepository.save(template.get());

        return templateField;
    }

    public Template deleteTemplate(UUID id, String user) {
        // Validate that the template still exists
        var template = templateRepository.findById(id);

        Validate.isTrue(template.isPresent(), "El template ya NO existe en el catálogo. No se actualizó nada.");

        templateRepository.deleteById(id);  // TODO check if delete fields and mapping

        eventService.sendEvent(UserContext.getCorrelationId(),user,
                "ELIMINA_TEMPLATE", template.get());
        getLogger().info("Delete a template" + template.get().getNombre() + " user:" + user);

        return template.get();
    }

    public Datasource addDatasource(DatasourceInput datasourceInput) {
        var datasource = Datasource.fromDTO(datasourceInput);

        eventService.sendEvent(UserContext.getCorrelationId(), datasourceInput.getUserModify(),
                "ANADE_NUEVO_DATASOURCE", datasourceInput);
        getLogger().info("Add a new datasource " + datasourceInput.getNombre());

        return datasourceRepository.save(datasource);
    }

    public Datasource updateDatasource(DatasourceInput datasourceInput)  {
        // Validate that the datasource already exists
        var datasource = datasourceRepository.findById(datasourceInput.getId());

        Validate.isTrue(datasource.isPresent(), "El datasource ya NO existe en el catálogo.");

        datasource.get().setNombre(datasourceInput.getNombre());
        datasource.get().setActivo(datasourceInput.getActivo());
        datasource.get().setJson(datasourceInput.getJson());
        datasource.get().setBlockly(datasourceInput.getBlockly());
        datasource.get().setConfig(datasourceInput.getConfig());
        datasource.get().setConfigBlockly(datasourceInput.getConfigBlockly());
        datasource.get().setMapping(datasourceInput.getMapping());
        datasource.get().setFechaModificacion(LocalDate.now());

        eventService.sendEvent(UserContext.getCorrelationId(), datasourceInput.getUserModify(),
                "MODIFICA_DATASOURCE", datasource.get());
        getLogger().info("Update an existing datasource " + datasource.get().getNombre());

        return datasourceRepository.save(datasource.get());
    }

    public DatasourceField addDatasourceField(UUID idDatasource, DatasourceFieldInput datasourceFieldInput)  {
        // Validate that the datasource already exists
        var datasource = datasourceRepository.findById(idDatasource);

        Validate.isTrue(datasource.isPresent(), "El datasource ya NO existe en el catálogo.");

        datasource.get().setFechaModificacion(LocalDate.now());

        var datasourceField = DatasourceField.fromDTO(datasourceFieldInput, datasource.get());


        datasource.get().getCampos().add(datasourceField);
        datasourceField.setDatasource(datasource.get());

        dataSourceFieldRepository.save(datasourceField);
        datasourceRepository.save(datasource.get());

        eventService.sendEvent(UserContext.getCorrelationId(), datasourceFieldInput.getUserModify(),
                "ANADE_DATASOURCE_FIELD", datasource.get());
        getLogger().info("Add a field " + datasourceFieldInput.getNombre() + " to the datasource " + datasource.get().getNombre());

        return datasourceField;
    }

    public DatasourceField updateDatasourceField(UUID idDatasource, DatasourceFieldInput datasourceFieldInput)  {
        // Validate that the datasource already exists
        var datasource = datasourceRepository.findById(idDatasource);

        Validate.isTrue(datasource.isPresent(), "El datasource ya NO existe en el catálogo.");

        DatasourceField datasourceField = null;

        for (DatasourceField ds : datasource.get().getCampos())
            if (ds.getId().equals(datasourceFieldInput.getId())) datasourceField = ds;

        Validate.isTrue(datasourceField != null, "El campo del data source NO existe en le catálogo");

        datasource.get().setFechaModificacion(LocalDate.now());
        datasourceField.setNombre(datasourceFieldInput.getNombre());
        datasourceField.setTipo(DatasourceField.FieldType.valueOf(datasourceFieldInput.getTipo()));
        datasourceField.setValidaciones(datasourceFieldInput.getValidaciones());

        eventService.sendEvent(UserContext.getCorrelationId(), datasourceFieldInput.getUserModify(),
                "ACTUALIZA_DATASOURCE_FIELD", datasource.get());
        getLogger().info("Update a field " + datasourceFieldInput.getNombre() + " to the datasource " + datasource.get().getNombre());

        datasourceRepository.save(datasource.get());

        return datasourceField;
    }

    public DatasourceField deleteDatasourceField(UUID idDatasource, UUID fieldId, String user)  {
        // Validate that the datasource already exists
        var datasource = datasourceRepository.findById(idDatasource);

        Validate.isTrue(datasource.isPresent(), "El datasource ya NO existe en el catálogo.");

        DatasourceField datasourceField = null;

        for (DatasourceField ds : datasource.get().getCampos())
            if (ds.getId().equals(fieldId)) datasourceField = ds;

        Validate.isTrue(datasourceField != null, "El campo del data source NO existe en le catálogo");

        datasource.get().setFechaModificacion(LocalDate.now());
        datasource.get().getCampos().remove(datasourceField);

        eventService.sendEvent(UserContext.getCorrelationId(), user,
                "ELIMINA_DATASOURCE_FIELD", datasource.get());
        getLogger().info("Delete a field " + datasourceField.getNombre() + " to the datasource " + datasource.get().getNombre());

        datasourceRepository.save(datasource.get());

        return datasourceField;
    }

    public Datasource deleteDatasource(UUID id, String user) {
        // Validate that the datasource still exists
        var datasource = datasourceRepository.findById(id);

        Validate.isTrue(datasource.isPresent(), "El datasource ya NO existe en el catálogo. No se actualizó nada.");

        datasourceRepository.deleteById(id);  // TODO check if delete fields and mapping

        eventService.sendEvent(UserContext.getCorrelationId(),user,
                "ELIMINA_DATASOURCE", datasource.get());
        getLogger().info("Delete a datasource" + datasource.get().getNombre() + " user:" + user);

        return datasource.get();
    }

    @NotNull
    @Override
    public Logger getLogger() { return HasLogger.DefaultImpls.getLogger(this); }
}

