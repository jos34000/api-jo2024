package dev.jos.back.mapper;

import dev.jos.back.dto.user.CreateUserDTO;
import dev.jos.back.dto.user.UserResponseDTO;
import dev.jos.back.entities.User;
import dev.jos.back.util.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private UserMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new UserMapper();
    }

    // ── toEntity ─────────────────────────────────────────────────────────────

    @Test
    void toEntity_mapsAllFields() {
        CreateUserDTO dto = new CreateUserDTO(
                "alice@jo2024.fr", "secret123", "Alice", "Dupont",
                true, Role.ROLE_ADMIN, "en"
        );

        User user = mapper.toEntity(dto);

        assertThat(user.getEmail()).isEqualTo("alice@jo2024.fr");
        assertThat(user.getFirstName()).isEqualTo("Alice");
        assertThat(user.getLastName()).isEqualTo("Dupont");
        assertThat(user.isMfaEnabled()).isTrue();
        assertThat(user.getRole()).isEqualTo(Role.ROLE_ADMIN);
        assertThat(user.getLocale()).isEqualTo("en");
    }

    @Test
    void toEntity_doesNotMapPassword() {
        CreateUserDTO dto = new CreateUserDTO(
                "alice@jo2024.fr", "secret123", "Alice", "Dupont", false, null, null
        );

        User user = mapper.toEntity(dto);

        assertThat(user.getPasswordHash()).isNull();
    }

    // ── toResponseDTO ─────────────────────────────────────────────────────────

    @Test
    void toResponseDTO_mapsAllFields() {
        User user = User.builder()
                .id(1L)
                .email("alice@jo2024.fr")
                .firstName("Alice")
                .lastName("Dupont")
                .mfaEnabled(true)
                .locale("fr")
                .role(Role.ROLE_USER)
                .build();

        UserResponseDTO dto = mapper.toResponseDTO(user);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.email()).isEqualTo("alice@jo2024.fr");
        assertThat(dto.firstName()).isEqualTo("Alice");
        assertThat(dto.lastName()).isEqualTo("Dupont");
        assertThat(dto.mfaEnabled()).isTrue();
        assertThat(dto.locale()).isEqualTo("fr");
        assertThat(dto.role()).isEqualTo(Role.ROLE_USER);
    }

    @Test
    void toResponseDTO_createdDateIsNullWhenNotPersisted() {
        User user = User.builder().id(2L).email("bob@jo2024.fr").build();

        UserResponseDTO dto = mapper.toResponseDTO(user);

        assertThat(dto.createdDate()).isNull();
    }
}
