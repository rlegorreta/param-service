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
 *  EventService.kt
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.paramservice.service

import com.ailegorreta.commons.event.*
import com.ailegorreta.commons.utils.HasLogger
import com.ailegorreta.paramservice.config.ServiceConfig
import com.ailegorreta.resourceserver.utils.UserContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.stereotype.Service


/**
 * EventService that sends events to the kafka machine.
 *
 *  @author rlh
 *  @project : param-service
 *  @date August 2023
 */
@Service
class EventService(private val streamBridge: StreamBridge,
                   private val serviceConfig: ServiceConfig,
                   private val mapper: ObjectMapper): HasLogger {

    private val coreName = "param" // By default, in this microservice all events go to
    // go to the 'iam' event channel Is other channels needed
    // this attribute will not be a constant value

    /**
     * Send the event directly to a Kafka microservice
     */
    fun sendEvent(correlationId: String? = UserContext.getCorrelationId(),
                  userName: String,
                  eventName: String,
                  value: Any): EventDTO {
        val eventBody = mapper.readTree(mapper.writeValueAsString(value))
        val parentNode = mapper.createObjectNode()

        // Add the permit where notification will be sent
        parentNode.put("notificaFacultad", "NOTIFICA_PARAM")
        parentNode.set<JsonNode>("datos", eventBody!!)
        logger.debug("Send event $value")

        var event = EventDTO(correlationId = correlationId ?: "NA",
            eventType = EventType.DB_STORE,
            username = userName,
            eventName = eventName,
            applicationName = serviceConfig.appName!!,
            coreName = coreName,
            eventBody = parentNode)

        streamBridge.send("producer-out-0", event)

        return event
    }
}