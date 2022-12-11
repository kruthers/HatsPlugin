package com.kruthers.hats.utils

class HatNotFoundException(name: String): Exception("Unable to find hat $name")
class NoPlayerFoundException: Exception("Unable to find player")
class ModelIdNotUnite(number: Int): Exception("Unable to add hat with custom model data $number. Number already in use")
