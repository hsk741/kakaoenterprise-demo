<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>

<body>
<h2>[프로필 정보]</h2>
    <ul>
        <li>닉네임 : ${sessionScope.memberAuthentication.nickname}</li>
        <li>이메일 : ${sessionScope.memberAuthentication.email}</li>
        <li>프로필 이미지</li>
    </ul>
    <p><img src="${sessionScope.memberAuthentication.profileImageUrl}" alt="프로필 이미지" /></p>

    <p>
        <a href="/oauth2/user/logout"><img src="images/logout.png" alt="로그아웃" width="80" height="35" /></a>
        &nbsp;&nbsp;&nbsp;
        <a href="/oauth2/user/${sessionScope.memberAuthentication.id}/unlink"><img src="images/unlink.png" alt="탈퇴" width="80" height="35" /></a>
    </p>
</body>
