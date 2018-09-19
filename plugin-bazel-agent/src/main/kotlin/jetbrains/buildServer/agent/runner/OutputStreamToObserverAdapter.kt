package jetbrains.buildServer.agent.runner

import com.intellij.openapi.util.text.StringUtil
import devteam.rx.*
import java.io.ByteArrayOutputStream
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset

class OutputStreamToObserverAdapter(
        size: Int,
        private val _eventType: ProcessEventType) : ByteArrayOutputStream(size), Observable<ProcessEvent> {

    private var _lastLine: String = ""
    private val _subject = subjectOf<ProcessEvent>()

    override fun write(array: ByteArray?, off: Int, len: Int) {
        super.write(array, off, len)

        if (array == null) {
            return
        }

        try {
            val text = _lastLine + StringUtil.convertLineSeparators(String(array, off, len, charset))
            _lastLine = ""
            var pos = 0;
            do {
                val index = text.indexOf('\n', pos)
                if (index >= 0) {
                    val line = text.substring(pos, index)
                    pos = index + 1

                    @Suppress("NON_EXHAUSTIVE_WHEN")
                    when (_eventType) {
                        ProcessEventType.StdOut -> _subject.onNext(StdOutProcessEvent(line))
                        ProcessEventType.StdErr -> _subject.onNext(StdErrProcessEvent(line))
                    }
                } else {
                    _lastLine = text.substring(pos, text.length)
                    pos = text.length
                }
            } while (pos < text.length)
        } catch (e: UnsupportedEncodingException) {
            throw RuntimeException("Unable to convert output to encoding $charset", e)
        }
    }

    override fun subscribe(observer: Observer<ProcessEvent>): Disposable {
        val subscription = _subject.subscribe(observer)
        return disposableOf {
            subscription.use {
                if (_lastLine.isNotEmpty()) {
                    @Suppress("NON_EXHAUSTIVE_WHEN")
                    when (_eventType) {
                        ProcessEventType.StdOut -> _subject.onNext(StdOutProcessEvent(_lastLine))
                        ProcessEventType.StdErr -> _subject.onNext(StdErrProcessEvent(_lastLine))
                    }
                }
            }
        }
    }

    companion object {
        private val charset = Charset.forName("UTF-8")
    }
}