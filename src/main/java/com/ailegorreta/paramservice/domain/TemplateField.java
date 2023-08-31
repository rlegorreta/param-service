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
 *  TemplateField.java
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.paramservice.domain;

import com.ailegorreta.paramservice.gql.types.TemplateFieldInput;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;

import java.util.Objects;
import java.util.UUID;

/**
 * This table details all the fields hat can participate in a template,
 *
 *  note: This class is defined as a java class in order to keep with lombok & Kotlin working together.
 *  see: https://www.baeldung.com/kotlin/lombok
 *
 * @project param-service
 * @autho rlh
 * @date August 2023
 */
@Entity
@Table(name = "template_fields")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateField {

    @Id
    @GeneratedValue
    private UUID id;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_template", referencedColumnName = "id", insertable = true, updatable = false)
    @ToString.Exclude
    private Template template;

    @Column(name = "name")
    private String nombre;

    @Column(name = "type")
    private FieldType tipo;

    @Column(name = "default_value")
    private String valorDefault;

    public static TemplateField fromDTO(TemplateFieldInput templateFieldInput, Template template) {
        return new TemplateField(templateFieldInput.getId(),
                                                    template,
                                                    templateFieldInput.getNombre(),
                                                    FieldType.valueOf(templateFieldInput.getTipo()),
                                                    templateFieldInput.getValorDefault());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        TemplateField that = (TemplateField) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public enum FieldType {
        Texto, Entero, Real, Fecha, ERROR;

        private String toMxGraph() {
            switch (this) {
                case Texto:
                case ERROR: return "String";
                case Entero: return "Integer";
                case Real: return "Double";
                case Fecha: return "Date";
            }
            return null;
        }
    }

}
