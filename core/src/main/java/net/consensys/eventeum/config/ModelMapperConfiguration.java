package net.consensys.eventeum.config;

import net.consensys.eventeum.utils.ModelMapperFactory;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ModelMapperConfiguration {

    @Bean
    public ModelMapper modelMapper(List<Converter> converters) {
        ModelMapper modelMapper = ModelMapperFactory.getInstance().getModelMapper();
        converters.forEach(modelMapper::addConverter);
        return modelMapper;
    }

}
