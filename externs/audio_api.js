/*
 * Copyright 2012 The Closure Compiler Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @fileoverview Definitions for the Web Audio API with webkit prefix.
 *   Not all classes are currently implemented.
 * @see http://www.w3.org/TR/webaudio/
 *
 * @externs
 */



/**
 * @constructor
 */
var webkitAudioContext = function() {};


/** @type {AudioDestinationNode} */
webkitAudioContext.prototype.destination;


/** @type {number} */
webkitAudioContext.prototype.sampleRate;


/** @type {number} */
webkitAudioContext.prototype.currentTime;


/** @type {AudioListener} */
webkitAudioContext.prototype.listener;


/**
 * webkitAudioContext.prototype.createBuffer() has 2 syntax:
 *   * Regular method:
 *     createBuffer = function(numberOfChannels, length, sampleRate) {};
 *
 *   * ArrayBuffer method:
 *     createBuffer = function(buffer, mixToMono) {};
 *
 * @param {number|ArrayBuffer} a
 * @param {number|boolean} b
 * @param {number=} sampleRate
 */
webkitAudioContext.prototype.createBuffer = function(a, b, sampleRate) {};


/**
 * @param {ArrayBuffer} audioData
 * @param {Function} successCallback
 * @param {Function=} errorCallback
 * @throws {DOMException}
 */
webkitAudioContext.prototype.decodeAudioData = function(audioData, successCallback,
    errorCallback) {};


/**
 * @return {AudioBufferSourceNode}
 */
webkitAudioContext.prototype.createBufferSource = function() {};


/**
 * @param {number} bufferSize
 * @param {number} numberOfInputs
 * @param {number} numberOfOuputs
 * @return {JavaScriptAudioNode}
 */
webkitAudioContext.prototype.createJavaScriptNode = function(bufferSize,
    numberOfInputs, numberOfOuputs) {};


/**
 * @return {RealtimeAnalyserNode}
 */
webkitAudioContext.prototype.createAnalyser = function() {};


/**
 * @return {AudioGainNode}
 */
webkitAudioContext.prototype.createGainNode = function() {};


/**
 * @param {number=} maxDelayTime
 * @return {DelayNode}
 */
webkitAudioContext.prototype.createDelayNode = function(maxDelayTime) {};


/**
 * @return {BiquadFilterNode}
 */
webkitAudioContext.prototype.createBiquadFilter = function() {};


/**
 * @return {webkitAudioPannerNode}
 */
webkitAudioContext.prototype.createPanner = function() {};


/**
 * @return {ConvolverNode}
 */
webkitAudioContext.prototype.createConvolver = function() {};


/**
 * @return {AudioChannelSplitter}
 */
webkitAudioContext.prototype.createChannelSplitter = function() {};


/**
 * @return {AudioChannelMerger}
 */
webkitAudioContext.prototype.createChannelMerger = function() {};


/**
 * @return {DynamicsCompressorNode}
 */
webkitAudioContext.prototype.createDynamicsCompressor = function() {};



/**
 * @constructor
 * @extends {AudioNode}
 */
var webkitAudioPannerNode = function() {};


/** @type {number} */
webkitAudioPannerNode.EQUALPOWER = 0;


/** @type {number} */
webkitAudioPannerNode.HRTF = 1;


/** @type {number} */
webkitAudioPannerNode.SOUNDFIELD = 2;


/** @type {number} */
webkitAudioPannerNode.LINEAR_DISTANCE = 0;


/** @type {number} */
webkitAudioPannerNode.INVERSE_DISTANCE = 1;


/** @type {number} */
webkitAudioPannerNode.EXPONENTIAL_DISTANCE = 2;


/** @type {number} */
webkitAudioPannerNode.prototype.panningModel;


/**
 * @param {number} x
 * @param {number} y
 * @param {number} z
 */
webkitAudioPannerNode.prototype.setPosition = function(x, y, z) {};


/**
 * @param {number} x
 * @param {number} y
 * @param {number} z
 */
webkitAudioPannerNode.prototype.setOrientation = function(x, y, z) {};


/**
 * @param {number} x
 * @param {number} y
 * @param {number} z
 */
webkitAudioPannerNode.prototype.setVelocity = function(x, y, z) {};


/** @type {number} */
webkitAudioPannerNode.prototype.distanceModel;


/** @type {number} */
webkitAudioPannerNode.prototype.refDistance;


/** @type {number} */
webkitAudioPannerNode.prototype.maxDistance;


/** @type {number} */
webkitAudioPannerNode.prototype.rolloffFactor;


/** @type {number} */
webkitAudioPannerNode.prototype.coneInnerAngle;


/** @type {number} */
webkitAudioPannerNode.prototype.coneOuterAngle;


/** @type {number} */
webkitAudioPannerNode.prototype.coneOuterGain;


/** @type {AudioGain} */
webkitAudioPannerNode.prototype.coneGain;


/** @type {AudioGain} */
webkitAudioPannerNode.prototype.distanceGain;

/*
 * Copyright 2012 The Closure Compiler Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @fileoverview Definitions for the Web Audio API.
 *   This file is based on the　Working Draft 15 March 2012.
 * @see http://www.w3.org/TR/webaudio/
 *
 * @externs
 */



/**
 * @constructor
 */
var AudioContext = function() {};


/** @type {AudioDestinationNode} */
AudioContext.prototype.destination;


/** @type {number} */
AudioContext.prototype.sampleRate;


/** @type {number} */
AudioContext.prototype.currentTime;


/** @type {AudioListener} */
AudioContext.prototype.listener;


/**
 * AudioContext.prototype.createBuffer() has 2 syntax:
 *   * Regular method:
 *     createBuffer = function(numberOfChannels, length, sampleRate) {};
 *
 *   * ArrayBuffer method:
 *     createBuffer = function(buffer, mixToMono) {};
 *
 * @param {number|ArrayBuffer} a
 * @param {number|boolean} b
 * @param {number=} sampleRate
 */
AudioContext.prototype.createBuffer = function(a, b, sampleRate) {};


/**
 * @param {ArrayBuffer} audioData
 * @param {Function} successCallback
 * @param {Function=} errorCallback
 * @throws {DOMException}
 */
AudioContext.prototype.decodeAudioData = function(audioData, successCallback,
    errorCallback) {};


/**
 * @return {AudioBufferSourceNode}
 */
AudioContext.prototype.createBufferSource = function() {};


/**
 * @param {number} bufferSize
 * @param {number} numberOfInputs
 * @param {number} numberOfOuputs
 * @return {JavaScriptAudioNode}
 */
AudioContext.prototype.createJavaScriptNode = function(bufferSize,
    numberOfInputs, numberOfOuputs) {};


/**
 * @return {RealtimeAnalyserNode}
 */
AudioContext.prototype.createAnalyser = function() {};


/**
 * @return {AudioGainNode}
 */
AudioContext.prototype.createGainNode = function() {};


/**
 * @param {number=} maxDelayTime
 * @return {DelayNode}
 */
AudioContext.prototype.createDelayNode = function(maxDelayTime) {};


/**
 * @return {BiquadFilterNode}
 */
AudioContext.prototype.createBiquadFilter = function() {};


/**
 * @return {AudioPannerNode}
 */
AudioContext.prototype.createPanner = function() {};


/**
 * @return {ConvolverNode}
 */
AudioContext.prototype.createConvolver = function() {};


/**
 * @return {AudioChannelSplitter}
 */
AudioContext.prototype.createChannelSplitter = function() {};


/**
 * @return {AudioChannelMerger}
 */
AudioContext.prototype.createChannelMerger = function() {};


/**
 * @return {DynamicsCompressorNode}
 */
AudioContext.prototype.createDynamicsCompressor = function() {};



/**
 * @constructor
 */
var AudioNode = function() {};


/**
 * @param {AudioNode} destination
 * @param {number=} output
 * @param {number=} input
 */
AudioNode.prototype.connect = function(destination, output, input) {};


/**
 * @param {number=} output
 */
AudioNode.prototype.disconnect = function(output) {};


/** @type {AudioContext} */
AudioNode.prototype.context;


/** @type {number} */
AudioNode.prototype.numberOfInputs;


/** @type {number} */
AudioNode.prototype.numberOfOutputs;



/**
 * @constructor
 * @extends {AudioNode}
 */
var AudioSourceNode = function() {};



/**
 * @constructor
 * @extends {AudioNode}
 */
var AudioDestinationNode = function() {};


/** @type {number} */
AudioDestinationNode.prototype.numberOfChannels;



/**
 * @constructor
 */
var AudioParam = function() {};


/** @type {number} */
AudioParam.prototype.value;


/** @type {number} */
AudioParam.prototype.maxValue;


/** @type {number} */
AudioParam.prototype.minValue;


/** @type {number} */
AudioParam.prototype.defaultValue;


/** @type {number} */
AudioParam.prototype.units;


/**
 * @param {number} value
 * @param {number} time
 */
AudioParam.prototype.setValueAtTime = function(value, time) {};


/**
 * @param {number} value
 * @param {number} time
 */
AudioParam.prototype.linearRampToValueAtTime = function(value, time) {};


/**
 * @param {number} value
 * @param {number} time
 */
AudioParam.prototype.exponentialRampToValueAtTime = function(value, time) {};


/**
 * @param {number} targetValue
 * @param {number} time
 * @param {number} timeConstant
 */
AudioParam.prototype.setTargetValueAtTime = function(targetValue, time,
    timeConstant) {};


/**
 * @param {Float32Array} values
 * @param {number} time
 * @param {number} duration
 */
AudioParam.prototype.setValueCurveAtTime = function(values, time, duration) {};


/**
 * @param {number} startTime
 */
AudioParam.prototype.cancelScheduledValues = function(startTime) {};



/**
 * @constructor
 * @extends {AudioParam}
 */
var AudioGain = function() {};



/**
 * @constructor
 * @extends {AudioNode}
 */
var AudioGainNode = function() {};


/** @type {AudioGain} */
AudioGainNode.prototype.gain;



/**
 * @constructor
 * @extends {AudioNode}
 */
var DelayNode = function() {};


/** @type {AudioParam} */
DelayNode.prototype.delayTime;



/**
 * @constructor
 */
var AudioBuffer = function() {};


/** @type {AudioGain} */
AudioBuffer.prototype.gain;


/** @type {number} */
AudioBuffer.prototype.sampleRate;


/** @type {number} */
AudioBuffer.prototype.length;


/** @type {number} */
AudioBuffer.prototype.duration;


/** @type {number} */
AudioBuffer.prototype.numberOfChannels;


/**
 * @param {number} channel
 * @return {Float32Array}
 */
AudioBuffer.prototype.getChannelData = function(channel) {};



/**
 * @constructor
 * @extends {AudioSourceNode}
 */
var AudioBufferSourceNode = function() {};


/** @type {AudioBuffer} */
AudioBufferSourceNode.prototype.buffer;


/** @type {AudioGain} */
AudioBufferSourceNode.prototype.gain;


/** @type {AudioParam} */
AudioBufferSourceNode.prototype.playbackRate;


/** @type {boolean} */
AudioBufferSourceNode.prototype.loop;


/**
 * @param {number} when
 */
AudioBufferSourceNode.prototype.noteOn = function(when) {};


/**
 * @param {number} when
 * @param {number} grainOffset
 * @param {number} grainDuration
 */
AudioBufferSourceNode.prototype.noteGrainOn = function(when, grainOffset,
    grainDuration) {};


/**
 * @param {number} when
 */
AudioBufferSourceNode.prototype.noteOff = function(when) {};



/**
 * @constructor
 * @extends {AudioSourceNode}
 */
var MediaElementAudioSourceNode = function() {};



/**
 * @constructor
 * @extends {AudioNode}
 */
var JavaScriptAudioNode = function() {};


/** @type {EventListener} */
JavaScriptAudioNode.prototype.onaudioprocess;


/** @type {number} */
JavaScriptAudioNode.prototype.bufferSize;



/**
 * @constructor
 * @extends {Event}
 */
var AudioProcessingEvent = function() {};


/** @type {JavaScriptAudioNode} */
AudioProcessingEvent.prototype.node;


/** @type {number} */
AudioProcessingEvent.prototype.playbackTime;


/** @type {AudioBuffer} */
AudioProcessingEvent.prototype.inputBuffer;


/** @type {AudioBuffer} */
AudioProcessingEvent.prototype.outputBuffer;



/**
 * @constructor
 * @extends {AudioNode}
 */
var AudioPannerNode = function() {};


/** @type {number} */
AudioPannerNode.EQUALPOWER = 0;


/** @type {number} */
AudioPannerNode.HRTF = 1;


/** @type {number} */
AudioPannerNode.SOUNDFIELD = 2;


/** @type {number} */
AudioPannerNode.LINEAR_DISTANCE = 0;


/** @type {number} */
AudioPannerNode.INVERSE_DISTANCE = 1;


/** @type {number} */
AudioPannerNode.EXPONENTIAL_DISTANCE = 2;


/** @type {number} */
AudioPannerNode.prototype.panningModel;


/**
 * @param {number} x
 * @param {number} y
 * @param {number} z
 */
AudioPannerNode.prototype.setPosition = function(x, y, z) {};


/**
 * @param {number} x
 * @param {number} y
 * @param {number} z
 */
AudioPannerNode.prototype.setOrientation = function(x, y, z) {};


/**
 * @param {number} x
 * @param {number} y
 * @param {number} z
 */
AudioPannerNode.prototype.setVelocity = function(x, y, z) {};


/** @type {number} */
AudioPannerNode.prototype.distanceModel;


/** @type {number} */
AudioPannerNode.prototype.refDistance;


/** @type {number} */
AudioPannerNode.prototype.maxDistance;


/** @type {number} */
AudioPannerNode.prototype.rolloffFactor;


/** @type {number} */
AudioPannerNode.prototype.coneInnerAngle;


/** @type {number} */
AudioPannerNode.prototype.coneOuterAngle;


/** @type {number} */
AudioPannerNode.prototype.coneOuterGain;


/** @type {AudioGain} */
AudioPannerNode.prototype.coneGain;


/** @type {AudioGain} */
AudioPannerNode.prototype.distanceGain;



/**
 * @constructor
 */
var AudioListener = function() {};


/** @type {number} */
AudioListener.prototype.gain;


/** @type {number} */
AudioListener.prototype.dopplerFactor;


/** @type {number} */
AudioListener.prototype.speedOfSound;


/**
 * @param {number} x
 * @param {number} y
 * @param {number} z
 */
AudioListener.prototype.setPosition = function(x, y, z) {};


/**
 * @param {number} x
 * @param {number} y
 * @param {number} z
 * @param {number} xUp
 * @param {number} yUp
 * @param {number} zUp
 */
AudioListener.prototype.setOrientation = function(x, y, z, xUp, yUp, zUp) {};


/**
 * @param {number} x
 * @param {number} y
 * @param {number} z
 */
AudioListener.prototype.setVelocity = function(x, y, z) {};



/**
 * @constructor
 * @extends {AudioNode}
 */
var ConvolverNode = function() {};


/** @type {AudioBuffer} */
ConvolverNode.prototype.buffer;


/** @type {boolean} */
ConvolverNode.prototype.normalize;



/**
 * @constructor
 * @extends {AudioNode}
 */
var RealtimeAnalyserNode = function() {};


/**
 * @param {Float32Array} array
 */
RealtimeAnalyserNode.prototype.getFloatFrequencyData = function(array) {};


/**
 * @param {Uint8Array} array
 */
RealtimeAnalyserNode.prototype.getByteFrequencyData = function(array) {};


/**
 * @param {Uint8Array} array
 */
RealtimeAnalyserNode.prototype.getByteTimeDomainData = function(array) {};


/** @type {number} */
RealtimeAnalyserNode.prototype.fftSize;


/** @type {number} */
RealtimeAnalyserNode.prototype.frequencyBinCount;


/** @type {number} */
RealtimeAnalyserNode.prototype.minDecibels;


/** @type {number} */
RealtimeAnalyserNode.prototype.maxDecibels;


/** @type {number} */
RealtimeAnalyserNode.prototype.smoothingTimeConstant;



/**
 * @constructor
 * @extends {AudioNode}
 */
var AudioChannelSplitter = function() {};



/**
 * @constructor
 * @extends {AudioNode}
 */
var AudioChannelMerger = function() {};



/**
 * @constructor
 * @extends {AudioNode}
 */
var DynamicsCompressorNode = function() {};


/** @type {AudioParam} */
DynamicsCompressorNode.prototype.threshold;


/** @type {AudioParam} */
DynamicsCompressorNode.prototype.knee;


/** @type {AudioParam} */
DynamicsCompressorNode.prototype.ratio;


/** @type {AudioParam} */
DynamicsCompressorNode.prototype.reduction;


/** @type {AudioParam} */
DynamicsCompressorNode.prototype.attack;


/** @type {AudioParam} */
DynamicsCompressorNode.prototype.release;



/**
 * @constructor
 * @extends {AudioNode}
 */
var BiquadFilterNode = function() {};


/** @type {number} */
BiquadFilterNode.LOWPASS = 0;


/** @type {number} */
BiquadFilterNode.HIGHPASS = 1;


/** @type {number} */
BiquadFilterNode.BANDPASS = 2;


/** @type {number} */
BiquadFilterNode.LOWSHELF = 3;


/** @type {number} */
BiquadFilterNode.HIGHSHELF = 4;


/** @type {number} */
BiquadFilterNode.PEAKING = 5;


/** @type {number} */
BiquadFilterNode.NOTCH = 6;


/** @type {number} */
BiquadFilterNode.ALLPASS = 7;


/** @type {number} */
BiquadFilterNode.prototype.type;


/** @type {AudioParam} */
BiquadFilterNode.prototype.frequency;


/** @type {AudioParam} */
BiquadFilterNode.prototype.Q;


/** @type {AudioParam} */
BiquadFilterNode.prototype.gain;


/**
 * @param {Float32Array} frequencyHz
 * @param {Float32Array} magResponse
 * @param {Float32Array} phaseResponse
 */
BiquadFilterNode.prototype.getFrequencyResponse = function(frequencyHz,
    magResponse, phaseResponse) {};



/**
 * @constructor
 * @extends {AudioNode}
 */
var WaveShaperNode = function() {};


/** @type {Float32Array} */
WaveShaperNode.prototype.curve;
