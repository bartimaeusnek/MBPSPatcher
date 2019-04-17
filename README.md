A mod that allows to patch mods with BPS files.
This is useful if you don't have the rights to publish modified versions of a mod, but still need to modify them for some reason.
I.e. for bug fixes or to port them to newer/older Minecraft versions.

To create a MBPS understandable file, first make a BPS file with Floating IPS (see https://github.com/Alcaro/Flips) and place that in the /MBPS/Patches folder.
Then either, if you got the right to provide the original file, place that in the /MBPS/Original folder, or add a notice for your Users to place it there.
And finally, edit the configuration of this mod, you can find it in /MBPS/config.cfg, and add the names of the original and of the patch to the "Patches" section.

The next time someone loads his game, this software will kick in, search the Patches folder for new Patches and if there are any and the original is present,
it will copy the name of the PatchFile and create a new file in the Mods folder, that is Patched by with the BPS file.

If you are publishing an update, simply add the old name of the patch to the UpdateList, and the new one to the Patch/Originals list as described above, this Programm
will automatically delete the old file and place the new file in the mods folder.

Information about BPS Files:
A BPS file contains nothing more than a list of changes to achieve the same results the patch author did, therefore it does not contain any Original Assets or Code.