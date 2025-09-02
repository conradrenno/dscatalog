package com.devrenno.dscatalog.services;

import com.devrenno.dscatalog.dto.RoleDTO;
import com.devrenno.dscatalog.dto.UserDTO;
import com.devrenno.dscatalog.dto.UserInsertDTO;
import com.devrenno.dscatalog.dto.UserUpdateDTO;
import com.devrenno.dscatalog.entities.Role;
import com.devrenno.dscatalog.entities.User;
import com.devrenno.dscatalog.repositories.RoleRepository;
import com.devrenno.dscatalog.repositories.UserRepository;
import com.devrenno.dscatalog.services.exceptions.DatabaseException;
import com.devrenno.dscatalog.services.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository repository;

    @Autowired
    private RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public Page<UserDTO> findAll(Pageable pageable) {
        Page<User> result = repository.findAll(pageable);
        Page<UserDTO> page = result.map(x -> new UserDTO(x));
        return page;
    }

    @Transactional(readOnly = true)
    public UserDTO findById(Long id) {
        User user = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Resource not found"));
        return new UserDTO(user);
    }

    @Transactional
    public UserDTO insert(UserInsertDTO dto) {
        User user = new User();
        dtoToUser(user, dto);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user = repository.save(user);
        return new UserDTO(user);
    }

    @Transactional
    public UserDTO update(Long id, UserUpdateDTO dto) {
        try {
            User user = repository.getReferenceById(id);
            dtoToUser(user, dto);
            user = repository.save(user);
            return new UserDTO(user);
        } catch (EntityNotFoundException e){
            throw new ResourceNotFoundException("Resource not found");
        }
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)){
            throw new ResourceNotFoundException("Resource not found");
        }
        try {
            repository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Integrity violation");
        }
    }

    private void dtoToUser(User user, UserDTO dto) {
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());

        user.getRoles().clear();
        for (RoleDTO roleDto : dto.getRoles()) {
            Role role = roleRepository.getReferenceById(roleDto.getId());
            user.getRoles().add(role);
        }

    }

}
