package com.shop.ClientServiceRest.Jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.shop.ClientServiceRest.Model.Client;
import com.shop.ClientServiceRest.Model.Role;

import java.io.IOException;

public class ClientSerializer extends StdSerializer<Client> {
    public ClientSerializer() {
        this(null);
    }

    protected ClientSerializer(Class<Client> t) {
        super(t);
    }

    @Override
    public void serialize(Client client, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();

        if (client.getId() == null) {
            jsonGenerator.writeNullField("id");
        } else {
            jsonGenerator.writeNumberField("id", client.getId());
        }
        jsonGenerator.writeStringField("firstName", client.getFirstName());
        jsonGenerator.writeStringField("lastName", client.getLastName());
        jsonGenerator.writeStringField("login", client.getLogin());
        jsonGenerator.writeStringField("password", client.getPassword());
        jsonGenerator.writeStringField("email", client.getEmail());
        if (client.getPatronymic() == null) {
            jsonGenerator.writeNullField("patronymic");
        } else {
            jsonGenerator.writeStringField("patronymic", client.getPatronymic());
        }

        if (client.getRoles() == null) {
            jsonGenerator.writeNullField("roles");
        } else {
            jsonGenerator.writeArrayFieldStart("roles");

            for (Role r : client.getRoles()) {
                jsonGenerator.writeString(r.name());
            }

            jsonGenerator.writeEndArray();
        }

        if (client.getConfirmationCode() == null) {
            jsonGenerator.writeNullField("confirmationCode");
        } else {
            jsonGenerator.writeStringField("confirmationCode", client.getConfirmationCode());
        }

        jsonGenerator.writeBooleanField("accountNonLocked", client.isAccountNonLocked());

        jsonGenerator.writeEndObject();
    }
}
