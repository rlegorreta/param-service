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
 *  SystemDate.java
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.paramservice.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * This table different date are stored. Today, re-process, yesterday, tomorrow, holidays, etc.
 *
 *  note: This class is defined as a java class in order to keep with lombok & Kotlin working together.
 *  see: https://www.baeldung.com/kotlin/lombok
 *
 * @project param-service
 * @autho rlh
 * @date August 2023
 */
/**
 * This table different date are stored. Today, re-process, yesterday, tomorrow, holidays, etc.
 *
 *  note: This class must be a java class in order that QueryDSL maven plugin can generate
 *        correctly the SystemDateQ class. Do not change this class to a kotlin class
 *
 * @project param-server-repo
 * @autho rlh
 * @date January 2023
 */
@Entity
@Table(name = "sys_dates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemDate {

    @Id
    @GeneratedValue
    private UUID id;

    @Enumerated(EnumType.STRING)
    private DayType     name;

    private LocalDate day;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        SystemDate that = (SystemDate) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }

}