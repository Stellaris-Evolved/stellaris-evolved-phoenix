import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.findPsiFile
import liveplugin.show
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parentOfType
import icu.windea.pls.script.psi.*
import liveplugin.ActionGroupIds
import liveplugin.PluginUtil.registerAction

// depends-on-plugin icu.windea.pls

show("Current project: ${project?.name}")
fun getFile(path: String, project: Project): PsiFile? {
    val projectBaseDir = project.basePath ?: return null
    val filePath = "$projectBaseDir/$path"
    return VirtualFileManager.getInstance().findFileByUrl("file://$filePath")?.findPsiFile(project)
}

fun copyTenetButtons(project: Project) {
    val targetFile = getFile("interface/zz_tec_tenet_view.gui", project) ?: return
    val toCopy = getFile("interface/z_tec_autogenerated_tenet_buttons.gui", project) ?: return
    var offset = targetFile.text.indexOf("\"tec_tenet_container\"")
    if (offset < 0) return

    val first = targetFile.findElementAt(targetFile.text.indexOf("# TENETS START HERE"))?.nextSibling
    var last = targetFile.findElementAt(targetFile.text.indexOf("# TENETS END HERE"))?.prevSibling

    if (first != null && last != null) targetFile.deleteChildRange(first, last)

    offset = toCopy.text.indexOf("guiTypes")

    if (offset < 0) return
    val block = toCopy.findElementAt(offset)?.parentOfType<ParadoxScriptProperty>()?.block
    block?.propertyList?.filter { it.name == "containerWindowType" }?.forEach {
        targetFile.addBefore(ParadoxScriptElementFactory.createLine(project), targetFile.findElementAt(targetFile.text.indexOf("# TENETS END HERE"))?.prevSibling)
        targetFile.addBefore(it, targetFile.findElementAt(targetFile.text.indexOf("# TENETS END HERE"))?.prevSibling)
    }
}

//

val pluginActionGroupId = "evolved.group"
val pluginActionGroup = DefaultActionGroup("Evolved", true)

pluginActionGroup.addTextOverride("MainMenu", "Evolved")
registerAction(pluginActionGroupId, "", ActionGroupIds.Menu.Tools, pluginActionGroup)

class EvolvedCopyTenetButtons : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return;
        WriteCommandAction.writeCommandAction(project).withName("Copy Tenet Buttons").run<Throwable> {
            copyTenetButtons(project)
        }
    }
}

val evolvedCopyTenetButtons = EvolvedCopyTenetButtons()
evolvedCopyTenetButtons.addTextOverride("MainMenu", "Copy Tenet Buttons")
registerAction("evolved.copy_tenet_buttons", "", pluginActionGroupId, evolvedCopyTenetButtons)