package nextstep.subway.acceptance;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

import static nextstep.subway.acceptance.AuthSteps.토큰_인증;
import static nextstep.subway.acceptance.FavouriteSteps.*;
import static nextstep.subway.acceptance.LineSteps.지하철_노선_생성_요청;
import static nextstep.subway.acceptance.MemberSteps.회원_생성_요청;
import static nextstep.subway.acceptance.StationSteps.지하철역_생성_요청;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("즐겨찾기 기능 인수테스트")
public class FavouriteAcceptanceTest extends AcceptanceTest {
    private static final String SOURCE = "source";
    private static final String TARGET = "target";

    private Long 사용자Id;
    private String 사용자토큰;
    private Long 강남역Id;
    private Long 역삼역Id;
    private Long 양재역Id;
    private Long 이호선Id;

    /**
     * Background : 사용자와 역이 등록되어져있다.
     */
    @BeforeEach
    void init() {
        사용자Id = extractId(회원_생성_요청("test@email.com", "1234", 26));
        사용자토큰 = 토큰_인증("test@email.com", "1234");

        강남역Id = extractId(지하철역_생성_요청("강남역"));
        역삼역Id = extractId(지하철역_생성_요청("역삼역"));
        양재역Id = extractId(지하철역_생성_요청("양재역"));

        이호선Id = extractId(지하철_노선_생성_요청(createParams("이호선", "green", 강남역Id, 역삼역Id, 13)));
    }

    private Long extractId(ExtractableResponse<Response> response) {
        return response.jsonPath().getLong("id");
    }

    private Map<String, String> createParams(String lineName, String lineColor, Long upStationId, Long downStationId, int distance) {
        Map<String, String> params = new HashMap<>();
        params.put("name", lineName);
        params.put("color", lineColor);
        params.put("upStationId", upStationId + "");
        params.put("downStationId", downStationId + "");
        params.put("distance", distance + "");

        return params;
    }

    /**
     * Scenario : 인증된 사용자가 즐겨찾기 기능을 사용한다.
     * <p>
     * <즐겨찾기 등록>
     * When     : 인증된 사용자가 두 역에 대해 즐겨찾기를 요청하면,
     * Then     : 선호 경로가 등록된다.
     * <p>
     * <즐겨찾기 조회>
     * When     : 인증된 사용자가 즐겨찾기 목록을 조회하면,
     * Then     : 즐겨찾기된 선호경로들이 조회된다.
     * <p>
     * <즐겨찾기 삭제>
     * When     : 인증된 사용자가 등록된 선호경로에 대해 즐겨찾기 취소를 요청하면,
     * Then     : 해당 선호경로에 대한 즐겨찾기가 취소된다.
     */
    @Test
    @DisplayName("인증된 회원이 즐겨찾기 기능을 수행한다.")
    void 즐겨찾기_기능() {
        /* 즐겨찾기 등록 */
        // when
        ExtractableResponse<Response> postResponse = 즐겨찾기_요청(사용자토큰, 강남역Id, 역삼역Id);
        // then
        응답_상태코드_검증(postResponse, HttpStatus.CREATED);
        assertThat(postResponse.header("Location")).isEqualTo("/favourites/1");

        /* 즐겨찾기 조회 */
        // when
        ExtractableResponse<Response> getResponse = 즐겨찾기_조회(사용자토큰);
        // then
        응답_상태코드_검증(getResponse, HttpStatus.OK);
        assertThat(getResponse.body().jsonPath().getString("[0].source.name")).isEqualTo("강남역");
        assertThat(getResponse.body().jsonPath().getString("[0].target.name")).isEqualTo("역삼역");

        /* 즐겨찾기 취소 */
        // when
        Long 선호경로Id = getResponse.body().jsonPath().getLong("[0].id");
        ExtractableResponse<Response> deleteResponse = 즐겨찾기_취소(사용자토큰, 선호경로Id);
        // then
        응답_상태코드_검증(deleteResponse, HttpStatus.NO_CONTENT);
    }

    private void 응답_상태코드_검증(ExtractableResponse<Response> postResponse, HttpStatus created) {
        assertThat(postResponse.statusCode()).isEqualTo(created.value());
    }

    /**
     * Scenario : 인증되지 않은 회원이 즐겨찾기 기능에 접근하면 에러가 발생된다.
     * When     : 인증을 수행하지 않은 체로 즐겨찾기 기능에 접근하면
     * Then     : 401 에러가 발생된다.
     */
    @Test
    @DisplayName("인증되지 않은 회원이 즐겨찾기를 접근하면 에러를 반환한다.")
    void 미인증_사용자_즐겨찾기_접근() {
        /** 즐겨찾기 요청 **/
        // when
        ExtractableResponse<Response> postResponse = FavouriteSteps.미인증_즐겨찾기_요청(강남역Id, 역삼역Id);
        // then
        응답_상태코드_검증(postResponse, HttpStatus.UNAUTHORIZED);

        /** 즐겨찾기 조회 **/
        // when
        ExtractableResponse<Response> getResponse = FavouriteSteps.미인증_즐겨찾기_조회();
        // then
        응답_상태코드_검증(getResponse, HttpStatus.UNAUTHORIZED);

        /** 즐겨찾기 취소 **/
        // when
        ExtractableResponse<Response> deleteResponse = FavouriteSteps.미인증_즐겨찾기_취소();
        // then
        응답_상태코드_검증(deleteResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Scenario : 다른 사람이 즐겨찾기한 선호경로에 대해 취소요청을 하면 403 에러가 발생된다.
     * Given    : 새로운 사용자를 등록하고
     * Given    : 새로운 사용자가 선호 경로를 즐겨찾기한다.
     * When     : 해당 선호경로에 대해 취소 요청을 하면
     * Then     : 403 권한 에러가 발생한다.
     */
    @Test
    @DisplayName("본인의 것이 아닌 선호경로에 대해 취소요청을 하면 에러를 반환한다.")
    void 즐겨찾기_되지않은_경로를_취소() {
        // given
        Long 새로운회원_id = extractId(회원_생성_요청("test2@email.com", "1234", 24));
        String 새로운회원_토큰 = 토큰_인증("test2@email.com", "1234");
        Long 새로운_회원의_선호경로Id = Long.valueOf(
                즐겨찾기_요청(새로운회원_토큰, 강남역Id, 역삼역Id)
                        .header("Location")
                        .split("/")[2]
        );

        // when
        ExtractableResponse<Response> 취소_response = 즐겨찾기_취소(사용자토큰, 새로운_회원의_선호경로Id);

        // then
        응답_상태코드_검증(취소_response, HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("없는 선호 경로에 대해 취소요청을 하면 에러를 반환한다.")
    void 없는_선호경로에_대한_취소() {
        // when
        ExtractableResponse<Response> deleteResponse = 즐겨찾기_취소(사용자토큰, 1929312L);

        // then
        응답_상태코드_검증(deleteResponse, HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("없는 역을 대상으로 즐겨찾기를 시도할 경우 에러를 반환한다.")
    void 없는_역에_대한_즐겨찾기_요청() {
        // when
        ExtractableResponse<Response> postResponse = 즐겨찾기_요청(사용자토큰, 강남역Id, 191919L);

        // then
        응답_상태코드_검증(postResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Scenario : 인증된 사용자가 이어지지 못하는 두 역을 즐겨찾기로 경로 등록을 하면 에러를 반환한다.
     * Given    : 새로운 역을 하나 만들고
     * When     : 새로운 역과 기존 노선의 역에 대해 즐겨찾기 요청을 시도하면
     * Then     : 400 에러를 반환한다.
     */
    @Test
    @DisplayName("이어지지 못하는 역들에 대해 즐겨찾기를 시도한 경우 에러를 반환한다.")
    void 이어지지_못하는_두_역에_대해_요청() {
        // given
        Long 용산역Id = extractId(지하철역_생성_요청("용산역"));
        Long 운정역Id = extractId(지하철역_생성_요청("운정역"));
        지하철_노선_생성_요청(createParams("경의중앙선", "blue", 용산역Id, 운정역Id, 10));

        // when
        ExtractableResponse<Response> postResponse = 즐겨찾기_요청(사용자토큰, 강남역Id, 용산역Id);

        // then
        응답_상태코드_검증(postResponse, HttpStatus.BAD_REQUEST);
    }
}