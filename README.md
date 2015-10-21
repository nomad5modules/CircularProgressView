# CircularProgressView

A circular progressview for android

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-CircularProgressView-brightgreen.svg?style=flat)](http://android-arsenal.com/details/1/1815)

## About

This is a simple small class that renders a progress in form of an filling arc shape.
A progress text can be rendered additionally. This text is rendered in the secondary color for the 'done' area of the arc.

I used it for rendering the progress that is done in the background of all views. This view has no dependencies and min SDK version 3 can be used.

It also supports a custom font (that is places in the 'assets' directory) and fading of the progress view (even though I recommend setting the 'fadeTime' value to -1 and use a Fragment with a FragmentTransation to fade this view in and out).

All values are optional. If you want to reuse the view from the code, simply set the class members in the CircularProgressView class accordingly.


![alt text](https://github.com/momentumlab/CircularProgressView/blob/master/demo.gif "Example")

## XML Attributes

Following XML attributes are allowed

```xml
<momentum.circularprogressview.CircularProgressView
	progressColor=		"#FFFFFF"
	progressText=		"TEST PROGRESS  #VAL#"
	progressTextFont=	"consolab.ttf"
	progressTextSize=	"36"
	borderOffset=		"150"
	fadeTime=			"500" />
```

progressColor:
> The main color of the progress ARC.
> The text will be rendered in this color where
> the arc is not yet painted

progressText:
> The Text to be rendered
> Add `#VAL#` to the text in order to automatically add a interger value to the progress text

progressTextFont:
> An optional ttf or otf font
> This font hast to be in the "assets" directory

progressTextSize:
> The font size

borderOffset:
> Optional border offset, next iteration will kick this,
> its just a fix to fill the corners (*will be deprecated in next version*)

fadeTimeMs:
> The fade in and fade out time, can be set to -1 to ignore fading

## Example Application
* An example application is included in the github repository. It can also be downloaded from the playstore here https://play.google.com/store/apps/details?id=momentum.circularprogress

## Precompiled AAR
You can download the latest precompiled version here http://jcenter.bintray.com/momentum/modules/circularprogressview/1.3.0/:circularprogressview-1.3.0.aar


## Additional information
* Set the progress to 0 (at the beginning) and 1 (at the end).
* 'wrapcontent' for dimensions is not yet supported
* Call `setProgress` with additional interpolation time (in ms) in order to interpolate between progress values

## License
Check file LICENSE and Make sure to follow the licensing terms and conditions of the project and the software used to build the project.

## Feedback
If you have any questions or feedback, feel free to get back to me!

## Next Todo's
* Add library to mavenCentral and jCenter. (Therefore a package renaming will be neccessary)