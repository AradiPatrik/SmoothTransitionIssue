# Issue description
When using concatenating media source the video freezes for a while the next video is loaded when using clipped video.
Using `ClippingConfiguration.setStartsAtKeyframe` solves this. I want our users to be able to set precise clipping for their edited videos thus I can't use this solution.

Below is a screen recording of this behavior:

https://github.com/user-attachments/assets/83b96de5-8456-4483-95f4-097d643cd538

Notice when the bunny cracks it's neck the frame freezes for around half a second. In my app I don't want this to happen.

I have been seaching github issues and have found this issue
* https://github.com/google/ExoPlayer/issues/10408

The proposed solution there was manually disabling codec flushing. I did as suggested but that seem to have just break things (the next video was blank, video player stuck)

Could you please provide guidance on how to start preloading the next media item while one is playing so transition can be smooth. Could you please give me a place to begin from?
Should I create custom MediaSources or Renderers? I saw there's a PreloadingMediaSource in the library can that help with this issue?
