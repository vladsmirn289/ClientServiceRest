package com.shop.ClientServiceRest.Jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.shop.ClientServiceRest.Model.Client;
import com.shop.ClientServiceRest.Model.Role;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ClientDeserializer extends StdDeserializer<Client> {
    public ClientDeserializer() {
        this(null);
    }

    protected ClientDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Client deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        String firstName = node.get("firstName").asText();
        String lastName = node.get("lastName").asText();
        String login = node.get("login").asText();
        String password = node.get("password").asText();
        String email = node.get("email").asText();
        Iterator<JsonNode> rolesNode = node.get("roles").elements();
        boolean accountNonLocked = node.get("accountNonLocked").asBoolean();

        Set<Role> roles = new HashSet<>();
        while (rolesNode.hasNext()) {
            roles.add(Role.valueOf(rolesNode.next().asText()));
        }

        Client client = new Client(email, password, firstName, lastName, login);
        if (node.hasNonNull("id")) {
            client.setId(node.get("id").asLong());
        }
        if (node.hasNonNull("patronymic")) {
            client.setPatronymic(node.get("patronymic").asText());
        }
        if (node.hasNonNull("confirmationCode")) {
            client.setConfirmationCode(node.get("confirmationCode").asText());
        }
        client.setRoles(roles);
        client.setNonLocked(accountNonLocked);

        return client;
    }
}
