package brs

import brs.entity.Arguments
import brs.objects.Props
import brs.util.logging.safeError
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.TextArea
import javafx.stage.Stage
import org.slf4j.LoggerFactory
import java.awt.*
import java.io.FileDescriptor
import java.io.OutputStream
import java.io.PrintStream
import java.net.InetAddress
import java.net.URI
import java.security.Permission
import kotlin.system.exitProcess

class BurstGUI : Application() {
    private var userClosed = false
    private lateinit var stage: Stage
    private var trayIcon: TrayIcon? = null

    private var burst: Burst? = null

    override fun start(primaryStage: Stage) {
        System.setSecurityManager(BurstGUISecurityManager())
        primaryStage.title = "Burst Reference Software version " + Burst.VERSION
        val textArea = LimitedTextArea(OUTPUT_MAX_LINES)
        textArea.isEditable = false
        sendJavaOutputToTextArea(textArea)
        primaryStage.scene = Scene(textArea, 800.0, 450.0)
        primaryStage.icons.add(javafx.scene.image.Image(javaClass.getResourceAsStream(ICON_LOCATION)))
        stage = primaryStage
        showTrayIcon()
        Thread { this.runBrs() }.start()
    }

    private fun shutdown() {
        userClosed = true
        if (trayIcon != null && SystemTray.isSupported()) {
            SystemTray.getSystemTray().remove(trayIcon)
        }
        exitProcess(0) // BRS shutdown handled by exit hook
    }

    private fun showTrayIcon() {
        if (trayIcon == null) { // Don't start running in tray twice
            trayIcon = createTrayIcon()
            if (trayIcon != null) {
                stage.setOnCloseRequest { hideWindow() }
            } else {
                stage.show()
                stage.setOnCloseRequest { shutdown() }
            }
        }
    }

    private fun createTrayIcon(): TrayIcon? {
        try {
            val systemTray = SystemTray.getSystemTray()
            val popupMenu = PopupMenu()

            val openWebUiButton = MenuItem("Open Web GUI")
            val showItem = MenuItem("Show BRS output")
            val shutdownItem = MenuItem("Shutdown BRS")

            openWebUiButton.addActionListener { openWebUi() }
            showItem.addActionListener { showWindow() }
            shutdownItem.addActionListener { shutdown() }

            popupMenu.add(openWebUiButton)
            popupMenu.add(showItem)
            popupMenu.add(shutdownItem)

            val newTrayIcon = TrayIcon(
                Toolkit.getDefaultToolkit().createImage(BurstGUI::class.java.getResource(ICON_LOCATION)),
                "Burst Reference Software",
                popupMenu
            )
            newTrayIcon.image = newTrayIcon.image.getScaledInstance(newTrayIcon.size.width, -1, Image.SCALE_SMOOTH)
            newTrayIcon.addActionListener { openWebUi() }
            systemTray.add(newTrayIcon)
            return newTrayIcon
        } catch (e: Exception) {
            logger.safeError(e) { "Could not create tray icon" }
            return null
        }
    }

    private fun showWindow() {
        Platform.runLater { stage.show() }
    }

    private fun hideWindow() {
        Platform.runLater { stage.hide() }
    }

    private fun openWebUi() {
        try {
            if (burst == null) {
                showMessage("BRS has not been initialized - cannot open Web UI")
                return
            }
            val propertyService = burst!!.dp.propertyService
            val port =
                if (propertyService.get(Props.DEV_TESTNET)) propertyService.get(Props.DEV_API_PORT) else propertyService.get(
                    Props.API_PORT
                )
            val httpPrefix = if (propertyService.get(Props.API_SSL)) "https://" else "http://"
            val address = httpPrefix + "localhost:" + port
            try {
                Desktop.getDesktop().browse(URI(address))
            } catch (e: Exception) { // Catches parse exception or exception when opening browser
                logger.safeError(e) { "Could not open browser" }
                showMessage("Error opening web UI. Please open your browser and navigate to $address")
            }
        } catch (e: Exception) { // Catches error accessing PropertyService
            logger.safeError(e) { "Could not access PropertyService" }
            showMessage("Could not open web UI as could not read BRS configuration.")
        }
    }

    private fun runBrs() {
        try {
            burst = Burst.init(Arguments.parse(parameters.raw.toTypedArray()))
            try {
                if (burst!!.dp.propertyService.get(Props.DEV_TESTNET)) {
                    onTestNetEnabled()
                }
            } catch (t: Exception) {
                logger.safeError(t) { "Could not determine if running in testnet mode" }
            }
        } catch (ignored: SecurityException) {
        } catch (t: Exception) {
            logger.safeError(t) { FAILED_TO_START_MESSAGE }
            showMessage(FAILED_TO_START_MESSAGE)
            onBrsStopped()
        }
    }

    private fun onTestNetEnabled() {
        Platform.runLater { stage.title = stage.title + " (TESTNET)" }
        trayIcon!!.toolTip = trayIcon!!.toolTip + " (TESTNET)"
    }

    private fun onBrsStopped() {
        Platform.runLater { stage.title = stage.title + " (STOPPED)" }
        trayIcon!!.toolTip = trayIcon!!.toolTip + " (STOPPED)"
    }

    private fun sendJavaOutputToTextArea(textArea: TextArea) {
        System.setOut(PrintStream(TextAreaOutputStream(textArea, System.out)))
        System.setErr(PrintStream(TextAreaOutputStream(textArea, System.err)))
    }

    private fun showMessage(message: String) {
        Platform.runLater {
            System.err.println("Showing message: $message")
            val dialog = Alert(Alert.AlertType.ERROR, message, ButtonType.OK)
            dialog.graphic = null
            dialog.headerText = null
            dialog.title = "BRS Message"
            dialog.show()
        }
    }

    private class TextAreaOutputStream internal constructor(
        private val textArea: TextArea?,
        private val actualOutput: PrintStream
    ) : OutputStream() {
        private val lineBuilder = StringBuilder()

        override fun write(b: Int) {
            writeString(String(byteArrayOf(b.toByte())))
        }

        override fun write(b: ByteArray) {
            writeString(String(b))
        }

        override fun write(b: ByteArray, off: Int, len: Int) {
            writeString(String(b, off, len))
        }

        private fun writeString(string: String) {
            lineBuilder.append(string)
            val line = lineBuilder.toString()
            if (line.contains("\n")) {
                actualOutput.print(line)
                if (textArea != null) {
                    Platform.runLater { textArea.appendText(line) }
                }
                lineBuilder.delete(0, lineBuilder.length)
            }
        }
    }

    private inner class BurstGUISecurityManager : SecurityManager() {
        override fun checkExit(status: Int) {
            if (!userClosed) {
                logger.safeError { "$UNEXPECTED_EXIT_MESSAGE $status" }
                Platform.runLater { stage.show() }
                showMessage("$UNEXPECTED_EXIT_MESSAGE $status")
                onBrsStopped()
                throw SecurityException()
            }
        }

        override fun checkPermission(perm: Permission) {
            // No need to check.
        }

        override fun checkPermission(perm: Permission, context: Any) {
            // No need to check.
        }

        override fun checkCreateClassLoader() {
            // No need to check.
        }

        override fun checkAccess(t: Thread) {
            // No need to check.
        }

        override fun checkAccess(g: ThreadGroup) {
            // No need to check.
        }

        override fun checkExec(cmd: String) {
            // No need to check.
        }

        override fun checkLink(lib: String) {
            // No need to check.
        }

        override fun checkRead(fd: FileDescriptor) {
            // No need to check.
        }

        override fun checkRead(file: String) {
            // No need to check.
        }

        override fun checkRead(file: String, context: Any) {
            // No need to check.
        }

        override fun checkWrite(fd: FileDescriptor) {
            // No need to check.
        }

        override fun checkWrite(file: String) {
            // No need to check.
        }

        override fun checkDelete(file: String) {
            // No need to check.
        }

        override fun checkConnect(host: String, port: Int) {
            // No need to check.
        }

        override fun checkConnect(host: String, port: Int, context: Any) {
            // No need to check.
        }

        override fun checkListen(port: Int) {
            // No need to check.
        }

        override fun checkAccept(host: String, port: Int) {
            // No need to check.
        }

        override fun checkMulticast(maddr: InetAddress) {
            // No need to check.
        }

        override fun checkPropertiesAccess() {
            // No need to check.
        }

        override fun checkPropertyAccess(key: String) {
            // No need to check.
        }

        override fun checkPrintJobAccess() {
            // No need to check.
        }

        override fun checkPackageAccess(pkg: String) {
            // No need to check.
        }

        override fun checkPackageDefinition(pkg: String) {
            // No need to check.
        }

        override fun checkSetFactory() {
            // No need to check.
        }

        override fun checkSecurityAccess(target: String) {
            // No need to check.
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(BurstGUI::class.java)

        private const val ICON_LOCATION = "/images/burst_overlay_logo.png"
        private const val FAILED_TO_START_MESSAGE = "BurstGUI caught exception starting BRS"
        private const val UNEXPECTED_EXIT_MESSAGE = "BRS Quit unexpectedly! Exit code"

        private const val OUTPUT_MAX_LINES = 500

        @JvmStatic
        fun main(args: Array<String>) {
            Platform.setImplicitExit(false)
            launch(BurstGUI::class.java, *args)
        }
    }

    class LimitedTextArea(private val maxNumberOfLines: Int) : TextArea() {
        override fun replaceText(start: Int, end: Int, text: String) {
            super.replaceText(start, end, text)
            while (getText().split('\n').toTypedArray().size > maxNumberOfLines) {
                val fle = getText().indexOf('\n')
                super.replaceText(0, fle + 1, "")
            }
            positionCaret(getText().length)
        }
    }
}
