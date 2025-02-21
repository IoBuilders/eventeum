package net.consensys.eventeum.dto.event.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import java.io.IOException;
import net.consensys.eventeum.dto.event.parameter.NumberParameter;
import org.springframework.boot.jackson.JsonComponent;

@JsonComponent
public class NumberParameterSerializer extends JsonSerializer<NumberParameter> {
  @Override
  public void serialize(
      NumberParameter value, JsonGenerator gen, SerializerProvider serializerProvider)
      throws IOException {
    gen.writeStartObject();
    writeFields(value, gen);
    gen.writeEndObject();
  }

  @Override
  public void serializeWithType(
      NumberParameter value, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer)
      throws IOException {

    WritableTypeId typeId =
        typeSer.writeTypePrefix(gen, typeSer.typeId(value, JsonToken.START_OBJECT));

    writeFields(value, gen);
    typeSer.writeTypeSuffix(gen, typeId);
  }

  private void writeFields(NumberParameter value, JsonGenerator gen) throws IOException {
    gen.writeStringField("type", value.getType());
    gen.writeStringField("value", value.getValueString());
  }
}
