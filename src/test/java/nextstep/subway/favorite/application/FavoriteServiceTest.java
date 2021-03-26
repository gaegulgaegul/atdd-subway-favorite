package nextstep.subway.favorite.application;

import nextstep.subway.favorite.domain.Favorite;
import nextstep.subway.favorite.domain.FavoriteRepository;
import nextstep.subway.favorite.dto.FavoriteResponse;
import nextstep.subway.member.domain.CustomUserDetails;
import nextstep.subway.station.application.StationService;
import nextstep.subway.station.dto.StationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FavoriteServiceTest {

    private final Long USER_ID = 1L;
    private final String EMAIL = "email@email.com";
    private final String PASSWORD = "password";

    private StationResponse 교대역;
    private StationResponse 강남역;

    @Mock
    private StationService stationService;

    @Mock
    private FavoriteRepository favoriteRepository;

    private FavoriteService favoriteService;
    private CustomUserDetails loginMember;

    @BeforeEach
    public void setUp(){
        교대역 = new StationResponse(1L, "교대역", LocalDateTime.now(), LocalDateTime.now());
        강남역 = new StationResponse(2L, "강남역", LocalDateTime.now(), LocalDateTime.now());

        this.loginMember = new CustomUserDetails(USER_ID, EMAIL, PASSWORD);
        this.favoriteService = new FavoriteService(stationService, favoriteRepository);
    }

    @DisplayName("등록된 즐겨찾기 없는 경우 조회하기")
    @Test
    void getNoFavorites() {
        // given
        when(favoriteRepository.findByMemberId(anyLong())).thenReturn(Arrays.asList());

        // when
        final List< FavoriteResponse > favoriteResponses = favoriteService.getFavorites(loginMember);

        // then
        assertThat(favoriteResponses).isNotNull();
        assertThat(favoriteResponses).isInstanceOf(List.class);
    }

    @DisplayName("즐겨찾기 조회하기")
    @Test
    void getFavorites() {
        // given
        when(favoriteRepository.findByMemberId(anyLong())).thenReturn(Arrays.asList(new Favorite(1L, 1L, 2L)));
        when(stationService.findAllStations()).thenReturn(Arrays.asList(교대역, 강남역));

        // when
        final List< FavoriteResponse > favoriteResponses = favoriteService.getFavorites(loginMember);

        // then
        assertThat(favoriteResponses.size()).isEqualTo(1);
        assertThat(favoriteResponses.get(0)).isInstanceOf(FavoriteResponse.class);
    }
}