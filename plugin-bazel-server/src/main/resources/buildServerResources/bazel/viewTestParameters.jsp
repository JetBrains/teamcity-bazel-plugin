<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.bazel.BazelParametersProvider"/>

<c:if test="${not empty propertiesBean.properties[params.testTargetsKey]}">
    <div class="parameter">
        Targets: <props:displayValue name="${params.testTargetsKey}"/>
    </div>
</c:if>