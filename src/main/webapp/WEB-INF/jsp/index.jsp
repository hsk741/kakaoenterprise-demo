<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>

<a id="custom-login-btn" href="javascript:loginWithKakao()">
    <img src="//k.kakaocdn.net/14/dn/btqCn0WEmI3/nijroPfbpCa4at5EIsjyf0/o.jpg" width="222"/>
</a>
<br/>
${msg}
<p id="token-result"></p>
<script type="text/javascript">
    function loginWithKakao() {
        location.href = "/oauth2/auth/kakao";
    }

    function getCookie(name) {
        const value = "; " + document.cookie;
        const parts = value.split("; " + name + "=");
        if (parts.length === 2) return parts.pop().split(";").shift();
    }
</script>