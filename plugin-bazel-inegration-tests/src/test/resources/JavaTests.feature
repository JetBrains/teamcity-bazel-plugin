Feature: Java tests support

    Scenario Outline: BES produce the importData containing tests results
        When add the build event service argument -l=<logging>
        When specify the command test
        And add the target :tests
        And run in JavaTests
        Then the exit code is 0
        And the stdErr output is empty
        And the result contains all service messages like
        | #          | type  | path       | flowId | timestamp |
        | importData | junit | .+test.xml | .+     | .+        |
        Examples:
            | logging |
            | Quiet   |
            | Normal  |
            | Trace   |