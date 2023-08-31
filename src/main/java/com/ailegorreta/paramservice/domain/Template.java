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
 *  Template.java
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.paramservice.domain;

import com.ailegorreta.paramservice.gql.types.TemplateInput;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.Hibernate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.*;
import java.util.*;

/**
 * Table where all templates are defined. The actual content of the template is stored in Alresco
 *
 *  note: This class is defined as a java class in order to keep with lombok & Kotlin working together.
 *  see: https://www.baeldung.com/kotlin/lombok
 *
 * @project param-server-repo
 * @autho rlh
 * @date August 2023
 */
@Entity
@Table(name = "templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Template {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "name") @NotNull
    private String      nombre;

    @Column(name = "file_repo") @NotNull
    private String      fileRepo;

    @Column(name = "channel")
    private DestinoType  destino;

    @Column(name = "json_code")
    private String  json;

    @Column(name = "blockly_blocks")
    private String blockly;

    @CreatedDate
    @Column(name = "creation_date")
    private LocalDate fechaCreacion;

    @LastModifiedDate
    @Column(name = "modification_date")
    private LocalDate fechaModificacion;

    @Column(name = "author")
    private String autor;

    @Column(name = "active")
    private Boolean activo;

    @Column(name = "fields")
    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @Builder.Default
    private Collection<TemplateField> campos = new ArrayList();

    public static Template fromDTO(TemplateInput templateInput ) {
        var template = new Template(templateInput.getId(),
                                    templateInput.getNombre(),
                                    templateInput.getFileRepo(),
                                    DestinoType.valueOfNull(templateInput.getDestino()),
                                    templateInput.getJson(),
                                    templateInput.getBlockly(),
                                    LocalDate.now(), // if this update operation this set is not valid
                                    LocalDate.now(),
                                    templateInput.getAutor(),
                                    templateInput.getActivo(),
                                    null);

        return template;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Template template = (Template) o;
        return id != null && Objects.equals(id, template.id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }

    public enum DestinoType {
        Email, Reporte, SMS, Web, Otro, NoDefinido;

        // Avoid null values
        public static DestinoType valueOfNull(String value) {
            if (value != null)
                return DestinoType.valueOf(value);
            return null;
        }
    }
}
