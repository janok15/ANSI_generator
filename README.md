# KoBraMUD ANSI Art Generator

I have been a content creator on one of the proto-MMO games called MUDs for over 20 years. A MUD is basically a purely text-based RPG, but most of them have the possibility to create colored text. As a little extra spice, I like to create images using text so the players can look at how an especially difficult opponent looks like, or to take in a beautiful view...

KoBraMUD ANSI Art Generator is a program created in Java which allows the user to create colorized ANSI/ASCII art from image data for use on muds, Linux terminals, mIRC, etc.

I created this program to make the process easier compared to what I previously did: an elaborate process involving using saving an image in Photoshop with a set palette, then converting that image with a custom program to get a textfile which was then converted to its final form inside the mud itself. With this program I only need to paste image data into the program, and then use copy to get the final form directly, which saves a lot of time.

If you just want to run this program, you can download the .jar file from the binary folder.

To use this program you can either load an image file (bmp, png, gif, jpg), or paste image data directly from the clipboard into the program. In essence, it uses the Floyd-Steinberg dithering algorithm and gives a background color to each fixed-width character. There is also the option to add a variable amount of colored ASCII art on top of that.

The palettes are not (yet) user-definable, but instead uses the proprietary KoBraMUD, and default Unix shell and mIRC palettes.

The program includes a few rudimentary image filters, as well as an emulation of the "Levels" functionality from Photoshop which I have found to be incredibly useful for massaging the image data until it looks the best. I usually leave everything at default, then use "Levels", and finally "Sharpen" once.

When you are happy with the result, you can either save the data into a textfile, or you can use Edit → Copy and paste it into the file of your choice.

Any suggestions can be sent to: jan.o.kruse@gmail.com
