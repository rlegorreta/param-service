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
 *  Datasource.java
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.paramservice.domain;

import com.ailegorreta.paramservice.gql.types.DatasourceInput;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.Hibernate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDate;
import java.util.*;

/**
 * This table stores the different datasource used by templates and by the ingestor to import data.
 *
 *  note: This class is defined as a java class in order to keep with lombok & Kotlin working together.
 *  see: https://www.baeldung.com/kotlin/lombok
 *
 * @project param-service
 * @autho rlh
 * @date August, 2023
 */
@Entity
@Table(name = "data_sources")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
//@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Datasource {

    @Id
    @GeneratedValue
    //  @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "name") @NotNull
    private String nombre;

    @Column(name = "json_code")
    private String  json;

    @Column(name = "blockly_block")
    private String blockly;

    @Column(name = "config_code")
    private String  config;

    @Column(name = "config_blockly_block")
    private String configBlockly;

    @Column(name = "mapping")
    private String mapping;

    @CreatedDate
    @Column(name = "creation_date") @NotNull
    private LocalDate fechaCreacion;

    @LastModifiedDate
    @Column(name = "modification_date") @NotNull
    private LocalDate fechaModificacion;

    @Column(name = "author")
    private String autor;

    @Column(name = "active")
    private Boolean activo;

    @Column(name = "fields")
    @OneToMany(mappedBy = "datasource", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @Builder.Default
    private List<DatasourceField> campos = new ArrayList();


    public static Datasource fromDTO(DatasourceInput datasourceInput ) {
        var datasource = new Datasource(datasourceInput.getId(),
                                        datasourceInput.getNombre(),
                                        datasourceInput.getJson(),
                                        datasourceInput.getBlockly(),
                                        datasourceInput.getConfig(),
                                        datasourceInput.getConfigBlockly(),
                                        datasourceInput.getMapping(),
                                        LocalDate.now(), // if this update operation this set is not valid
                                        LocalDate.now(),
                                        datasourceInput.getAutor(),
                                        datasourceInput.getActivo(),
                                        null);  // the fields are just for update mapping

        return datasource;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Datasource that = (Datasource) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }

}
