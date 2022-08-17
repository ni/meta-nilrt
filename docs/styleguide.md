Before you read this document, you should familiarize yourself with the upstream [yocto project styleguide](https://www.openembedded.org/wiki/Styleguide). meta-nilrt roughly follows the upstream guide, except where the rules in this document disagree.


# General

**Capitalize NI LinuxRT and NILRT**

When referring to "NI LinuxRT" in recipes and documentation, or its abbreviated form: "NILRT", capitalize the N-I-L-R-and-T. The exception to this rule is when speaking about the "meta-nilrt" layer itself, or the "nilrt" git repository - where it is recommended you call it "nilrt.git".

**Indentation**

Use **tabs** for indentation and **spaces** for alignment in all places - *except* within python task functions, where spaces should be used for both.

The above policy differs from [yocto upstream's policy](https://www.openembedded.org/wiki/Styleguide#Format_Guidelines). Keep that in mind, if you are contributing both to meta-nilrt and to an upstream layer.

**Multiline Variable Assignment**

When recipe variables contain more than one *element*, the assignment should be written across multiple lines. This style greatly reduces the *noise* in git commits, and makes for easier rebases.

Further, meta-nilrt prefers to use **tabs** for indentation and follow this style...

```
VARIABLE = "\
	value_one \
	value_two \
"
```

The `DESCRIPTION` variable is generally exempt from these rules, since it is normally modified completely within git commits.


## Where to make recipe changes?

OE recipe metadata is distributed across bitbake layers within the project, and it is generally important to the health of the distribution that recipe changes are made in the appropriate layers.

If your recipe changes are appropriate for the OpenEmbedded community as a whole, they should be submitted to the appropriate community-layer upstream first. Once accepted by upstream, they should be cherry-picked back into the NI-owned fork of the layer.

If your recipe changes are specific to NI LinuxRT, they should be made in the `meta-nilrt` layer.

Keep in mind that it might be most-correct to split your patchset changes across layers, if part of the changes are OE-generic and part are NILRT-specific.


# Recipes

## Recipe Names

* Recipes should be named to match their upstream project name, unless it conflicts with an existing recipe. Recipes whose source is contained entirely within meta-nilrt can be freely named.
* Recipes which contain NI (National Instruments) content should be prefixed with `ni-`. eg. `ni-foobar.bb`.
* Recipe files whose source is contained entirely within meta-nilrt should *not* encode a package version in the recipe file name. Instead, they should explicitly declare a `PV` variable within the recipe. This rule allows the recipe version to be increased without introducing a rename into the git commit history.
    ```
	ni-foobar.bb      # good
	ni-foobar_1.0.bb  # bad
	```

## Layout

When laying out the variables within a recipe `.bb` or `.bbappend` file, generally follow the styling recommended by the [yocto wiki styleguide](https://www.openembedded.org/wiki/Styleguide#Style_Guidelines).

Recipe variables are generally divided into 6 sections, outlined below.
<details>

1. *Recipe Metadata*
    * `SUMMARY`
	* `DESCRIPTION`
	* `AUTHOR`
	* `HOMEPAGE`
	* `BUGTRACKER`
	* `SECTION`
	* `LICENSE`
	* `LIC_FILES_CHKSUM`
2. *Recipe Variables*
    * `DEPENDS`
	* `PROVIDES`
	* `PV`
2. *Source Variables*
    * `SRC_URI`
	* `SRCREV`
	* `S`
3. *Class Variables*
    * `inherit ...`
	* `PACKAGECONFIG`
	* build class specific variables, i.e. `EXTRA_QMAKEVARS_POST`, `EXTRA_OECONF`
4. *Task Variables*
    * task overrides, ie. `do_configure`
5. *Package Variables*
    * `PACKAGEARCH`
	* `PACKAGES`
	* `FILES`
	* `RDEPENDS`
	* `RRECOMMENDS`
	* `RSUGGESTS`
	* `RPROVIDES`
	* `RCONFLICTS`
6. *Class Extensions*
    * `BBCLASSEXTEND`

</details>

Contributors are free to use a single newline to create logical breaks between dissimilar variables *within* a section, and are encouraged to use double-newlines between each section.


## Variables

### Metadata

**BUGTRACKER**
* Set to the URL of the upstream project's bugtracker.
    * If the recipe source lives in the meta-nilrt layer directly, do not set this variable. Users can infer that the bugtracker is the `HOMEPAGE` - which should be set to the meta-nilrt upstream.

**DESCRIPTION**
* can be multiline.
* should describe the package in complete sentences.
* Prefer the expanded "NI LinuxRT" form, to maximize users' understanding.

**HOMEPAGE**
* Set to the URL of the project upstream.
    * If the recipe source lives in the meta-nilrt layer directly, set the homepage to the meta-nilrt canonical GH repo. `HOMEPAGE = "https://github.com/ni/meta-nilrt"`

**SUMMARY**
* Limit SUMMARY to 80 characters.
* Prefer the abbreviated "NILRT" form, to conserve characters.


# .patch Files

[Example of a good .patch file commit.](https://github.com/ni/meta-nilrt/pull/50/commits/73b046c57d73e188a3bf4adbf0965aa9312ebe08)

`.patch` files should include the original author's commit message and meta information at their top. Information about the OE context (like why the `.patch` file is needed for the recipe) should be added after the original author's commit.

At a minimum, you should add an additional message trailer declaring the [upstream status](https://www.openembedded.org/wiki/Commit_Patch_Message_Guidelines#Patch_Header_Recommendations:_Upstream-Status) of the .patch file at the time of your OE commit. These status lines are crucial to helping the project maintainers properly upgrade and rebase recipes. Common status lines are:

* `Upstream-Status: Inappropriate [$rationale]` ([ex](https://github.com/ni/meta-nilrt/blob/nilrt/master/sumo/recipes-support/curl/curl/0005-Add-nicurl-wrapper-functions.patch)) For when the `.patch` change is specific to NI LinuxRT and would not be desired (or has been rejected) by upstream.
* `Upstream-Status: Not Submitted [$rationale]` ([ex](https://github.com/ni/meta-nilrt/blob/nilrt/master/sumo/recipes-gnome/florence/files/0004-Add-option-for-automatic-bring-to-top.patch)) For when the `.patch` file is being included in NILRT before being submitted to its upstream project. This should only occur if the `.patch` is needed for an immediate NILRT release and there is no time to get it reviewed upstream beforehand. Or if - as in the case of the example - the upstream repo is dead.
* `Upstream-Status: Submitted [$upstream_link]` ([ex](https://github.com/ni/meta-nilrt/blob/1e6453ebca8735de96eaaf3bc931d22998c8dfb3/recipes-support/curl/curl/0014-Fixup-lib1529-test.patch)) For when the `.patch` has been submitted to the upstream project by mailing list or merge PR, but needs to be pulled into NILRT prior to final upstream approval.
* `Upstream-Status: Accepted [$upstream_link]` ([ex](https://github.com/ni/meta-nilrt/blob/904bd00bf24d8fe61d3a13b8ece368c9741a73fc/recipes-devtools/opkg/files/0002-libopkg-clear-curl-properties-on-download-error-to-p.patch#L36)) For when the `.patch` has been approved and pulled by the upstream project.


#### .patch file names

When bitbake applies `.patch` files to a recipe, it copies all `.patch` files into the recipe's workspace, then applies them in alphanumeric-order. In your PR, be mindful of how your `.patch` file is ordered versus the other files in the recipe. Keep in mind that some `.patch` files might come from other layers.


# Example Recipe
This is an example recipe for a package whose source is contained in the meta-nilrt layer.

`.../foobar/`
```
foobar/
|-  files/
|   |- foo-file.1
|   |- foo-file.2
|   \- foo.initd
\-  foobar.bb
```
----
`.../foobar/foobar.bb`
```
SUMMARY = "foobar - The example project"
DESCRIPTION = "\
foobar is an example project, used when you need to communicate concepts to \
developers."
HOMEPAGE = "https://github.com/ni/meta-nilrt"
SECTION = "test"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"


PV = "1.0"


SRC_URI = "\
	file://foo-file.1 \
	file://foo-file.2 \
	file://foo.initd \
"

S = "${WORKDIR}"


inherit update-rc.d
INITSCRIPT_PARAMS = "default"
INITSCRIPT = "foo"


prefix_lib=${libdir}/${BPN}

do_install () {
	install -d ${D}${sysconfdir}/init.d
	install foo.initd ${D}${sysconfdir}/init.d/foo

	install -d ${D}${prefix_lib}
	install --mode=0755 foo-file.1 ${D}${prefix_lib}/foo-file.1
	install --mode=0744 foo-file.2 ${D}${prefix_lib}/foo-file.2
}


RDEPENDS:${PN} = "bash"
```