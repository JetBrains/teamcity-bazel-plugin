<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.bazel.BazelParametersProvider"/>

<c:if test="${not empty propertiesBean.properties[params.cleanTargetsKey]}">
    <div class="parameter">
        Targets: <props:displayValue name="${params.cleanTargetsKey}"/>
    </div>
</c:if>