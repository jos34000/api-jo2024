package dev.jos.back.mapper;

import dev.jos.back.dto.cart.CartResponseDTO;
import dev.jos.back.entities.Cart;
import dev.jos.back.entities.CartItems;
import dev.jos.back.entities.Event;
import dev.jos.back.entities.Offer;
import dev.jos.back.support.TestFixtures;
import dev.jos.back.util.enums.CartStatus;
import dev.jos.back.util.enums.Phases;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class CartMapperTest {

    private CartMapper mapper;

    private static final LocalDateTime EXPIRES_AT = LocalDateTime.of(2024, 8, 1, 23, 59);

    @BeforeEach
    void setUp() {
        mapper = new CartMapper();
    }

    private CartItems item(double unitPrice, int quantity, int offerSeats) {
        Event event = TestFixtures.event("100m Finale", 50);
        Offer offer = TestFixtures.offer("Solo", unitPrice, offerSeats);

        CartItems item = new CartItems();
        item.setId(1L);
        item.setUnitPrice(unitPrice);
        item.setQuantity(quantity);
        item.setEvent(event);
        item.setOffer(offer);
        return item;
    }

    private Cart cart(CartItems... items) {
        Cart cart = new Cart();
        cart.setId(42L);
        cart.setStatus(CartStatus.ACTIVE);
        cart.setExpiresAt(EXPIRES_AT);
        cart.setCartItems(Set.of(items));
        return cart;
    }

    // ── toCartResponseDTO ─────────────────────────────────────────────────────

    @Test
    void toCartResponseDTO_mapsBasicFields() {
        Cart cart = cart();

        CartResponseDTO dto = mapper.toCartResponseDTO(cart);

        assertThat(dto.id()).isEqualTo(42L);
        assertThat(dto.status()).isEqualTo(CartStatus.ACTIVE);
        assertThat(dto.expiresAt()).isEqualTo(EXPIRES_AT);
    }

    @Test
    void toCartResponseDTO_handlesEmptyCart() {
        Cart cart = cart();

        CartResponseDTO dto = mapper.toCartResponseDTO(cart);

        assertThat(dto.totalPrice()).isEqualTo(0.0);
        assertThat(dto.totalTickets()).isEqualTo(0);
        assertThat(dto.items()).isEmpty();
    }

    @Test
    void toCartResponseDTO_computesTotalPriceAsSumOfSubtotals() {
        // item1 : 2 × 50.0 = 100.0 ; item2 : 3 × 30.0 = 90.0 → total = 190.0
        CartItems item1 = item(50.0, 2, 1);
        item1.setId(1L);
        CartItems item2 = item(30.0, 3, 1);
        item2.setId(2L);

        CartResponseDTO dto = mapper.toCartResponseDTO(cart(item1, item2));

        assertThat(dto.totalPrice()).isCloseTo(190.0, within(0.01));
    }

    @Test
    void toCartResponseDTO_computesTotalTicketsFromOfferSeats() {
        // 2 commandes × 2 places par offre = 4 billets
        CartItems item = item(80.0, 2, 2);

        CartResponseDTO dto = mapper.toCartResponseDTO(cart(item));

        assertThat(dto.totalTickets()).isEqualTo(4);
    }

    @Test
    void toCartResponseDTO_mapsItemEventAndOfferSummaries() {
        CartItems item = item(50.0, 1, 1);

        CartResponseDTO dto = mapper.toCartResponseDTO(cart(item));

        assertThat(dto.items()).hasSize(1);
        assertThat(dto.items().getFirst().event().name()).isEqualTo("100m Finale");
        assertThat(dto.items().getFirst().event().location()).isEqualTo("Stade de France");
        assertThat(dto.items().getFirst().event().phase()).isEqualTo(Phases.FINALE);
        assertThat(dto.items().getFirst().offer().name()).isEqualTo("Solo");
        assertThat(dto.items().getFirst().offer().price()).isEqualTo(50.0);
    }

    @Test
    void toCartResponseDTO_computesItemSubtotal() {
        CartItems item = item(40.0, 3, 1);

        CartResponseDTO dto = mapper.toCartResponseDTO(cart(item));

        assertThat(dto.items().getFirst().subtotal()).isCloseTo(120.0, within(0.01));
    }
}
