# HACK: this is a workaround for the following error because SRCPV is very long for this recipe
# by design in OE and nibuild doesn't like it because it exports to windows fileshares:
#
# The file <bla> which needs to be exported as <bla> has exceeded windows path limit of 260 characters.
# Please revert the export and rebuild the component with shorter paths.

PV = "1.0"
