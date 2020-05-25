Feature: My First Test
  Background:
    Given open google
Scenario Outline: Search in google
  When we search for text <searchText>
  Then we can see text <valueFound>
  Examples:
  |searchText|valueFound|
  |democracy|democracy|
  |sudy|sudy|

