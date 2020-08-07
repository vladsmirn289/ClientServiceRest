package com.shop.ClientServiceRest.Jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.shop.ClientServiceRest.Model.Item;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ItemSerializer extends StdSerializer<Item> {
    public ItemSerializer() {
        this(null);
    }

    protected ItemSerializer(Class<Item> t) {
        super(t);
    }

    @Override
    public void serialize(Item item, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        jsonGenerator.writeStartObject();

        if (item.getId() == null) {
            jsonGenerator.writeNullField("id");
        } else {
            jsonGenerator.writeNumberField("id", item.getId());
        }

        jsonGenerator.writeStringField("name", item.getName());
        jsonGenerator.writeNumberField("count", item.getCount());
        jsonGenerator.writeNumberField("weight", item.getWeight());
        jsonGenerator.writeNumberField("price", item.getPrice());
        jsonGenerator.writeStringField("description", item.getDescription());
        jsonGenerator.writeStringField("characteristics", item.getCharacteristics());
        jsonGenerator.writeStringField("image", item.getImage());
        jsonGenerator.writeStringField("code", item.getCode());
        jsonGenerator.writeObjectField("category", item.getCategory());

        if (item.getCreatedOn() == null) {
            jsonGenerator.writeStringField("createdOn", LocalDateTime.now().format(formatter));
        } else {
            jsonGenerator.writeStringField("createdOn", item.getCreatedOn().format(formatter));
        }

        jsonGenerator.writeEndObject();
    }
}
