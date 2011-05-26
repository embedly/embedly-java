Feature: Objectify

    As an embedly user
    I want to call the the embedly api
    Because I want to objectify a url

    Scenario Outline: Get the meta description with pro
        Given an embedly host with key
        When objectify is called with the <url> URL
        Then the meta.description should start with <metadesc>

        Examples:
            | url                            | metadesc                 |
            | http://yfrog.com/h855mugj      | yfrog free image and vid |
