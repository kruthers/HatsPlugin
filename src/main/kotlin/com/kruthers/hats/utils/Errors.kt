package com.kruthers.hats.utils

class NoPlayerFoundException: Exception("Unable to find player")
class ModelIdNotUnique(number: Int): Exception("Unable to add hat with custom model data $number. Number already in use")
