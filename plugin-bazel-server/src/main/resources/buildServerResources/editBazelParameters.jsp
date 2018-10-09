<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.bazel.BazelParametersProvider"/>
<jsp:useBean id="teamcityPluginResourcesPath" scope="request" type="java.lang.String"/>

<link rel="stylesheet" href="${teamcityPluginResourcesPath}bazel-settings.css">
<script type="text/javascript">
    var commandId = BS.Util.escapeId('${params.commandKey}');

    BS.ProjectDataPopup.insertSelectedValue = function(val, targetFieldId) {
        $j(BS.Util.escapeId(targetFieldId)).val(val).change();
        this.hidePopup(0);
    };

    BS.BazelParametersForm = {
        clearInputValues: function(row) {
            $j(row).find(':input').each(function(id, element) {
                var $element = $j(element);
                var name = $element.attr("name");
                if (!name || name.indexOf("prop:") !== 0) {
                    return;
                }
                var changed = false;
                if (element.name === "select") {
                    changed = element.selectedIndex !== 0;
                    element.selectedIndex = 0;
                } else if (element.type === "checkbox") {
                    changed = $element.is(':checked');
                    $element.removeAttr('checked');
                } else {
                    changed = $element.val() !== '';
                    $element.val('');
                }
                if (changed) {
                    $element.change();
                }
            });
        },
        updateElements: function () {
            var commandName = $j(commandId).val().trim().replace(" ", "-");

            $j("tr.bazel").each(function(id, element) {
                var $row = $j(element);
                if (!$row.hasClass(commandName)) {
                    $row.hide();
                    BS.BazelParametersForm.clearInputValues($row);
                } else {
                    $row.show();
                }
            });
            $j(".runnerFormTable span.error").empty();

            BS.MultilineProperties.updateVisible();
        }
    };

    // Use delay while waiting user typing
    var timer, delay = 500;
    $j(document).on('change keyup input paste', commandId, function () {
        clearTimeout(timer);
        timer = setTimeout(function() {
            BS.BazelParametersForm.updateElements();
        }, delay );
    });

    $j(document).on('ready', commandId, function () {
        BS.BazelParametersForm.updateElements();
    });
</script>

<tr>
    <th><label for="${params.commandKey}">Command:</label></th>
    <td>
        <props:textProperty name="${params.commandKey}" className="longField"/>
        <bs:projectData type="BazelCommands" sourceFieldId="${params.workingDirKey}"
                        targetFieldId="${params.commandKey}" popupTitle="Select command"
                        selectionMode="single" />
        <span class="error" id="error_${params.commandKey}"></span>
    </td>
</tr>

<props:workingDirectory/>

<tr class="bazel build clean run test">
    <th><label for="${params.targetsKey}">Target:</label></th>
    <td>
        <props:textProperty name="${params.targetsKey}" className="longField"/>
        <bs:projectData type="BazelTargets" sourceFieldId="${params.workingDirKey}"
                        targetFieldId="${params.targetsKey}" popupTitle="Select targets"
                        selectionMode="multiple"/>
        <span class="error" id="error_${params.targetsKey}"></span>
        <span class="smallNote">Specify the list of command targets.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th><label for="${params.argumentsKey}">Startup options:</label></th>
    <td>
        <props:textProperty name="${params.startupOptionsKey}" className="longField" expandable="true"/>
        <span class="error" id="error_${params.startupOptionsKey}"></span>
        <span class="smallNote">Enter additional options that appear before the command.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th><label for="${params.argumentsKey}">Command line parameters:</label></th>
    <td>
        <props:textProperty name="${params.argumentsKey}" className="longField" expandable="true"/>
        <span class="error" id="error_${params.argumentsKey}"></span>
        <span class="smallNote">Enter additional command line parameters for bazel.</span>
    </td>
</tr>

<tr class="advancedSetting" id="logging">
    <th><label for="${params.verbosityKey}">Logging verbosity:</label></th>
    <td>
        <props:selectProperty name="${params.verbosityKey}" enableFilter="true" className="mediumField">
            <props:option value="">&lt;Default&gt;</props:option>
            <c:forEach var="item" items="${params.verbosityValues}">
                <props:option value="${item.id}"><c:out value="${item.description}"/></props:option>
            </c:forEach>
        </props:selectProperty>
        <span class="error" id="error_${params.verbosityKey}"></span>
    </td>
</tr>

<script type="text/javascript">
    BS.BazelParametersForm.updateElements();
</script>