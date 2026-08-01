package com.builder.url_shortener.mapper;

import com.builder.url_shortener.dto.ShortUrlDto;
import com.builder.url_shortener.entity.ShortUrl;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ShortUrlMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "clickCount", defaultValue = "0L")
    ShortUrl toEntity(ShortUrlDto dto);

    ShortUrlDto toDto(ShortUrl entity);
}
