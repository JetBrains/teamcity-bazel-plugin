package jetbrains.buildServer.bazel

import bazel.BinaryFileBazelEventStream
import bazel.Event
import bazel.Verbosity
import bazel.bazel.converters.BazelEventConverter
import bazel.messages.ControllerSubject
import bazel.messages.HierarchyImpl
import bazel.messages.MessageFactoryImpl
import bazel.messages.StreamSubject
import com.intellij.openapi.diagnostic.Logger
import devteam.rx.Disposable
import devteam.rx.disposableOf
import devteam.rx.observer
import jetbrains.buildServer.agent.runner.BuildStepContext
import java.nio.file.Path

class BazelBinaryFileWatcher(
    private val _buildStepContext: BuildStepContext
) {
    var disposable: Disposable? = null
    var eventFile: Path? = null

    fun start() {
        LOG.info("Event file \"$eventFile\"")
        val file = eventFile
        if(file == null) { return }

        LOG.info("Start watching the Bazel binary file \"$file\"")

        val messageFactory = MessageFactoryImpl()
        val hierarchy = HierarchyImpl()

        val verbosity = Verbosity.Diagnostic
        val controllerSubject = ControllerSubject(verbosity, messageFactory, hierarchy) {
            StreamSubject(verbosity, messageFactory, hierarchy)
        }

        val logger = _buildStepContext.runnerContext.build.buildLogger
        val sub2 = controllerSubject.subscribe(observer(
            onNext = { logger.message(it.toString()) },
            onError = { logger.error(it.message ?: it.toString()) },
            onComplete = { }
        ))

        val binaryFileBazelEventStream = BinaryFileBazelEventStream(BazelEventConverter())
        val sub1 = binaryFileBazelEventStream.create(file).subscribe(observer(
            onNext = { controllerSubject.onNext(Event("", it))} ,
            onError = { controllerSubject.onError(it) },
            onComplete = { controllerSubject.onComplete() }
        ))

        disposable = disposableOf(sub1, sub2)
    }

    fun stop() {
        LOG.info("Stop watching the Bazel binary file")
        disposable?.dispose()
    }

    companion object {
        private val LOG = Logger.getInstance(BazelBinaryFileWatcher::class.java.name)
    }
}

