import { NavLink, useParams } from "react-router-dom";
import "./style/Hboard.css";
import Axios from "axios";
import React, { useEffect, useState } from "react";
import axiosIns from "../../api/JwtConfig";

function Hboard(props) {
  // const Hboard = () => {
  //   const handleRefresh = () => {
  //     window.location.reload();
  //   };

  const handleRefresh = () => {
    window.location.reload();
  };

  const [hireBoardList, setHireBoardList] = useState([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);

  const [contentCount, setContentCount] = useState(15); //텍스트의 초기 개수

  const handleResize = () => {
    // 화면 너비에 따라 텍스트 개수를 업데이트
    const screenWidth = window.innerWidth;
    if (screenWidth >= 1000) {
      setContentCount(80);
    } else if (screenWidth >= 768) {
      setContentCount(45);
    } else if (screenWidth >= 600) {
      setContentCount(35);
    } else if (screenWidth >= 480) {
      setContentCount(25);
    } else {
      setContentCount(15);
    }
  };

  useEffect(() => {
    // 컴포넌트가 마운트되거나 화면 크기가 변경될 때 리사이즈 이벤트 핸들러 등록
    window.addEventListener("resize", handleResize);
    handleResize(); // 초기 렌더링 시 텍스트 개수 설정

    return () => {
      // 컴포넌트가 언마운트될 때 리사이즈 이벤트 핸들러 제거
      window.removeEventListener("resize", handleResize);
    };
  }, []);

  useEffect(() => {
    fetchHboards(currentPage);
  }, [currentPage]);

  const fetchHboards = (page) => {
    axiosIns
      .get("/api/hboard/D0", { params: { page: page - 1 } })
      .then((response) => {
        setHireBoardList(response.data.hireBoardList);
        setTotalPages(response.data.totalPages);
      })
      .catch((error) => {
        console.error("Error fetching hboards:", error);
      });
  };

  useEffect(() => {
    // JPA로부터 데이터 가져오는 API 호출
    axiosIns
      .get("/api/hboard/D0")
      .then((response) => {
        setHireBoardList(response.data.hireBoardList);
      })
      .catch((error) => {
        console.error("Error fetching hboards:", error);
      });
  }, []);

  const goToPreviousPage = () => {
    if (currentPage > 1) {
      setCurrentPage(currentPage - 1);
    }
  };

  const goToNextPage = () => {
    if (currentPage < totalPages) {
      setCurrentPage(currentPage + 1);
    }
  };

  const timeForToday = (value) => {
    if (!value) {
      return "";
    }

    const valueConv = value.slice(0, -10);
    const today = new Date();
    const timeValue = new Date(valueConv);

    // timeValue를 한국 시간대로 변환
    const timeValueUTC = new Date(timeValue.toISOString());
    const offset = timeValue.getTimezoneOffset() * 60 * 1000; // 분 단위를 밀리초 단위로 변환
    const timeValueKST = new Date(timeValueUTC.getTime() - offset);

    const betweenTime = Math.floor(
      (today.getTime() - timeValueKST.getTime()) / 1000 / 60
    );
    if (betweenTime < 1) return "방금 전";
    if (betweenTime < 60) {
      return `${betweenTime}분 전`;
    }
    console.log(betweenTime);

    const betweenTimeHour = Math.floor(betweenTime / 60);
    if (betweenTimeHour < 24) {
      return `${betweenTimeHour}시간 전`;
    }

    const betweenTimeDay = Math.floor(betweenTime / 60 / 24);
    if (betweenTimeDay < 8) {
      return `${betweenTimeDay}일 전`;
    }

    const year = String(timeValue.getFullYear()).slice(0, 4);
    const month = String(timeValue.getMonth() + 1).padStart(2, "0");
    const day = String(timeValue.getDate()).padStart(2, "0");

    const formattedDateWithoutTime = `${year}-${month}-${day}`;

    return formattedDateWithoutTime;
  };

  return (
    <div className="hboard">
      <div className="advertise-box">
        <div className="advertise-main" />
        <b className="advertise-text">광고</b>
      </div>
      <div className="hboard-name">
        <div className="hboard-name-box" />
        <div className="hboard-name-text">
          <b className="hboard-name-text-type">채용정보 게시판</b>
          <div className="hboard-name-text-detail">
            다양한 기업들의 공고를 확인할 수 있는 게시판
          </div>
        </div>
      </div>
      <div className="hboard-selection">
        <div className="hboard-selection-freeboard">
          <div className="board-selection-freeboard-box" />
          <div className="board-selection-freeboard-text">자유</div>
        </div>
        <NavLink
          to="/qboard"
          activeClassName="active"
          className="fboard-selection-qna"
        >
          <div className="hboard-selection-qna-box" />
          <div className="hboard-selection-qna-text">{`Q&A`}</div>
        </NavLink>
        <div className="hboard-selection-hire">
          <div className="hboard-selection-hire-box" />
          <div className="hboard-selection-hire-text">채용정보</div>
        </div>
        <NavLink
          to="/aboard"
          activeClassName="active"
          className="fboard-selection-academy"
        >
          <div className="hboard-selection-qna-box" />
          <div className="hboard-selection-academy-text">학원별</div>
        </NavLink>
      </div>
      <div className="hboard-write">
        <div className="hboard-write-box" />
        <img
          className="hboard-write-icon"
          alt=""
          src={require("./assets/hboard_write_icon.svg").default}
        />
        <div className="hboard-write-text">글쓰기</div>
      </div>
      <div className="hboard-function-search-input" />
      <img
        className="hboard-function-search-icon"
        alt=""
        src={require("./assets/hboard_function_search_icon.svg").default}
      />
      {/* <img className="board-hr-icon" alt="" src="/board-hr.svg" /> */}
      <img
        className="hboard-pages-reset-icon"
        alt=""
        src={require("./assets/hboard_pages_reset.svg").default}
        onClick={handleRefresh}
      />
      <div className="hboard-pages">
        <div className="hboard-pages-current">{`${currentPage} / ${totalPages} 페이지`}</div>
        <img
          className="hboard-pages-back-icon"
          alt=""
          src={require("./assets/hboard_pages_back.svg").default}
          onClick={goToPreviousPage}
          style={{ opacity: currentPage === 1 ? 0.5 : 1 }}
        />
        <img
          className="hboard-pages-forward-icon"
          alt=""
          src={require("./assets/hboard_pages_forward.svg").default}
          onClick={goToNextPage}
          style={{ opacity: currentPage === totalPages ? 0.5 : 1 }}
        />
      </div>

      <img className="board-hr-icon1" alt="" src="/board-hr1.svg" />
      <div className="hboard-notice">
        <div className="hboard-notice-box" />
        <div className="hboard-notice-preview">
          <div className="hboard-notice-preview-info">
            <img
              className="board-notice-preview-info-logo-icon"
              alt=""
              src="/board-notice-preview-info-logo.svg"
            />
            <div className="hboard-notice-preview-info-tex">
              admin_01 · 약 4시간 전
            </div>
          </div>
          <b className="hboard-notice-preview-subject">DEVSTER 공지사항</b>
          <div className="hboard-notice-preview-notice">
            <div className="hboard-notice-preview-notice-b" />
            <div className="hboard-notice-preview-notice-t">공지사항</div>
          </div>
          <div className="hboard-notice-preview-hash">#공지사항 # Devster</div>
          <div className="hboard-notice-preview-icons">
            <div className="hboard-notice-preview-views">
              <div className="hboard-notice-preview-views-te">800</div>
              <img
                className="hboard-notice-preview-views-ic-icon"
                alt=""
                src={
                  require("./assets/hboard_notice_preview_views_icon.svg")
                    .default
                }
              />
            </div>
            <div className="hboard-notice-preview-icons-co">
              <div className="hboard-notice-preview-views-te">99</div>
              <img
                className="hboard-notice-preview-icons-co2"
                alt=""
                src={
                  require("./assets/hboard_notice_preview_icons_comments_icon.svg")
                    .default
                }
              />
            </div>
            <div className="hboard-notice-preview-icons-li">
              <div className="hboard-notice-preview-icons-li1">9</div>
              <img
                className="hboard-notice-preview-icons-li2"
                alt=""
                src={
                  require("./assets/hboard_notice_preview_icons_likes_icon.svg")
                    .default
                }
              />
            </div>
          </div>
        </div>
      </div>

      <div className="hboard_list">
        {hireBoardList &&
          hireBoardList.map((hboard) => (
            <div key={hboard.hboard.hb_idx} className="hboard-preview">
              <div className="hboard-preview-box" />
              <div className="fboard-preview-img-profile">
                <img alt="" src={hboard.cmPhoto} />
              </div>
              <div className="hboard-preview-type">
                <b className="hboard-preview-type-text">채용게시판</b>
                <div className="hboard-preview-type-date">
                  {timeForToday(hboard.hboard.hb_writeday)}
                </div>
              </div>
              <div className="hboard-preview-id">
                <div className="hboard-preview-type-text">
                  {hboard.cmNicname}
                </div>
              </div>
              <NavLink
                to={"/hboard/detail/${hboard.hboard.hb_idx}/${currentPage}"}
              >
                <b className="hboard-preview-subject">
                  {hboard.hboard.hb_subject}
                </b>
                <div className="hboard-preview-contents">
                  {hboard.hboard.hb_content.slice(0, contentCount)}
                </div>
                <div className="hboard-preview-img-preview">
                  <img alt="" src={hboard.hboard.hb_photo} />
                </div>
              </NavLink>

              <div className="hboard-preview-likes">
                <div className="hboard-preview-likes-text">
                  {hboard.hboard.hb_like - hboard.hboard.hb_dislike}
                </div>
                <img
                  className="hboard-preview-likes-icon"
                  alt=""
                  src={
                    require("./assets/hboard_preview_likes_icon.svg").default
                  }
                />
              </div>
              <div className="hboard-preview-comments">
                <div className="hboard-preview-likes-text">99</div>
                <img
                  className="hboard-preview-comments-icon"
                  alt=""
                  src={
                    require("./assets/hboard_preview_comments_icon.svg").default
                  }
                />
              </div>
              <div className="hboard-preview-views">
                <div className="hboard-preview-views-text">
                  {hboard.hboard.hb_readcount}
                </div>
                <img
                  className="hboard-preview-views-icon"
                  alt=""
                  src={
                    require("./assets/hboard_preview_views_icon.svg").default
                  }
                />
              </div>
            </div>
          ))}
      </div>

      {/* <div className="hboard-preview">
        <div className="hboard-preview-box" />
        <div className="hboard-preview-img-profile" />
        <div className="hboard-preview-type">
          <b className="hboard-preview-type-text">게시판명길이최대로</b>
          <div className="hboard-preview-type-date">작성시간</div>
        </div>
        <div className="hboard-preview-id">
          <div className="hboard-preview-type-text">아이디명최대로</div>
        </div>
        <b className="hboard-preview-subject">제목 일이삼사오육칠팔구...</b>
        <div className="hboard-preview-contents">
          본문 일이삼사오육칠팔구십일이...
        </div>
        <div className="hboard-preview-img-preview" />
        <div className="hboard-preview-likes">
          <div className="hboard-preview-likes-text">99.9k</div>
          <img
            className="hboard-preview-likes-icon"
            alt=""
            src={require("./assets/hboard_preview_likes_icon.svg").default}
          />
        </div>
        <div className="hboard-preview-comments">
          <div className="hboard-preview-likes-text">99.9k</div>
          <img
            className="hboard-preview-comments-icon"
            alt=""
            src={require("./assets/hboard_preview_comments_icon.svg").default}
          />
        </div>
        <div className="hboard-preview-views">
          <div className="hboard-preview-views-text">99.9k</div>
          <img
            className="hboard-preview-views-icon"
            alt=""
            src={require("./assets/hboard_preview_views_icon.svg").default}
          />
        </div>
      </div>
      <div className="hboard-function-sort">
        <div className="hboard-function-sort-box" />
        <div className="hboard-function-sort-time">최신순</div>
        <div className="hboard-function-sort-view">조회순</div>
        <div className="hboard-function-sort-like">인기순</div>
        <img
          className="hboard-function-sort-bar2-icon"
          alt=""
          src={require("./assets/hboard_function_sort_bar2.svg").default}
        />
        <img
          className="hboard-function-sort-bar-icon"
          alt=""
          src={require("./assets/hboard_function_sort_bar.svg").default}
        />
      </div>
      <div className="hboard-pages">
        <div className="hboard-pages-current">1 / 5 페이지</div>
        <img
          className="hboard-pages-back-icon"
          alt=""
          src={require("./assets/hboard_pages_back.svg").default}
        />
        <img
          className="hboard-pages-forward-icon"
          alt=""
          src={require("./assets/hboard_pages_forward.svg").default}
        />
      </div>
      <div className="hboard-pages1">
        <div className="hboard-pages-current">1 / 5 페이지</div>
        <img
          className="hboard-pages-back-icon"
          alt=""
          src={require("./assets/hboard_pages_back.svg").default}
        />
        <img
          className="hboard-pages-forward-icon"
          alt=""
          src={require("./assets/hboard_pages_forward.svg").default}
        />
      </div> */}
    </div>
  );
}

export default Hboard;
