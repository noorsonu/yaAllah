package com.main.mappers;

import java.util.List;

import org.mapstruct.Mapper;

import com.main.dtos.UserDto;
import com.main.entities.UserEntity;

@Mapper(componentModel = "spring")
public interface UserMapper {

	UserEntity toUser(UserDto dto);

	UserDto dtoToUser(UserEntity entity);

	List<UserDto> toListUserDto(List<UserEntity> list);

}

