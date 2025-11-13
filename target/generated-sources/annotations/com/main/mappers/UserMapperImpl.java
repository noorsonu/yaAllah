package com.main.mappers;

import com.main.dtos.UserDto;
import com.main.entities.UserEntity;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-13T12:53:46+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.43.0.v20250819-1513, environment: Java 21.0.8 (Eclipse Adoptium)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserEntity toUser(UserDto dto) {
        if ( dto == null ) {
            return null;
        }

        UserEntity userEntity = new UserEntity();

        userEntity.setEmail( dto.getEmail() );
        userEntity.setId( dto.getId() );
        userEntity.setName( dto.getName() );
        userEntity.setPassword( dto.getPassword() );
        userEntity.setRole( dto.getRole() );

        return userEntity;
    }

    @Override
    public UserDto dtoToUser(UserEntity entity) {
        if ( entity == null ) {
            return null;
        }

        UserDto userDto = new UserDto();

        userDto.setEmail( entity.getEmail() );
        userDto.setId( entity.getId() );
        userDto.setName( entity.getName() );
        userDto.setPassword( entity.getPassword() );
        userDto.setRole( entity.getRole() );

        return userDto;
    }

    @Override
    public List<UserDto> toListUserDto(List<UserEntity> list) {
        if ( list == null ) {
            return null;
        }

        List<UserDto> list1 = new ArrayList<UserDto>( list.size() );
        for ( UserEntity userEntity : list ) {
            list1.add( dtoToUser( userEntity ) );
        }

        return list1;
    }
}
