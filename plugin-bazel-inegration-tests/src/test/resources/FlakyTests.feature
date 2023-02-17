# Copyright 2000-2023 JetBrains s.r.o.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

Feature: Flaky tests support

    Scenario: BES produce a build problem when flaky_test_attempts were not specified at all
        When specify the command test
        And add the argument --test_env=test_id=#id
        And add the target :tests
        And run in FlakyTests
        Then the exit code is 3
        And the stdErr output is empty
        And the result contains all service messages like
        | #            | description  |
        | buildProblem | Build failed |

    Scenario: BES produce a build problem when sufficient flaky_test_attempts were specified
        When specify the command test
        And add the argument --test_env=test_id=#id
        And add the argument --flaky_test_attempts
        And add the argument 1
        And add the target :tests
        And run in FlakyTests
        Then the exit code is 3
        And the stdErr output is empty
        And the result contains all service messages like
            | #            | description  |
            | buildProblem | Build failed |

    Scenario Outline: BES does not produce a build problem when sufficient flaky_test_attempts were specified
        When specify the command test
        And add the argument --test_env=test_id=#id
        And add the argument --flaky_test_attempts
        And add the argument <flaky_test_attempts>
        And add the target :tests
        And run in FlakyTests
        Then the exit code is 0
        And the stdErr output is empty
        And the result does not contain any service messages like
            | #            | description  |
            | buildProblem | Build failed |
        Examples:
            | flaky_test_attempts |
            | 2                   |
            | 4                   |