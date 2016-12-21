SUMMARY = "OpenSAF is an open source implementation of the SAF AIS specification"
DESCRIPTION = "OpenSAF is an open source project established to develop a base platform \
middleware consistent with Service Availability Forum (SA Forum) \
specifications, under the LGPLv2.1 license. The OpenSAF Foundation was \
established by leading Communications and Enterprise Computing Companies to \
facilitate the OpenSAF Project and to accelerate the adoption of the OpenSAF \
code base in commercial products. \
The OpenSAF project was launched in mid 2007 and has been under development by \
an informal group of supporters of the OpenSAF initiative. The OpenSAF \
Foundation was founded on January 22nd 2008 with Emerson Network Power, \
Ericsson, Nokia Siemens Networks, HP and Sun Microsystems as founding members."
HOMEPAGE = "http://www.opensaf.org"
SECTION = "admin"
LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://COPYING.LIB;md5=a916467b91076e631dd8edb7424769c7"

DEPENDS = "libxml2 python"

SRC_URI = "${SOURCEFORGE_MIRROR}/${BPN}/releases/${BPN}-${PV}.tar.gz \
           file://install-samples-from-srcdir.patch \
           file://0001-plmcd-error-fix.patch \
           "

SRC_URI[md5sum] = "821391311c73bc2f2dc5a7d867f732e0"
SRC_URI[sha256sum] = "f687c883a0d50517c33cb445d9177a9e2b07f5bb077bcc0235ffc0fc99af5cb6"

inherit autotools useradd systemd pkgconfig

USERADD_PACKAGES = "${PN}"
GROUPADD_PARAM_${PN} = "-f -r opensaf"
USERADD_PARAM_${PN} =  "-r -g opensaf -d ${datadir}/opensaf/ -s ${sbindir}/nologin -c \"OpenSAF\" opensaf"

SYSTEMD_SERVICE_${PN} += "opensafd.service plmcboot.service plmcd.service"
SYSTEMD_AUTO_ENABLE = "disable"

PACKAGECONFIG[systemd] = "--enable-systemd-daemon"
PACKAGECONFIG[openhpi] = "--with-hpi-interface=B03 --enable-ais-plm,,openhpi"

EXTRA_OECONF += " --libdir=${libdir}/opensaf "
EXTRA_OEMAKE += " -Wl,-rpath,${libdir}/opensaf "

PKGLIBDIR="${libdir}/opensaf/opensaf"

do_configure_prepend () {
        ( cd ${S}; autoreconf -f -i -s )
}

do_install_append() {
    rm -fr "${D}${localstatedir}/lock"
    rm -fr "${D}${localstatedir}/run"
    rmdir --ignore-fail-on-non-empty "${D}${localstatedir}"
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${B}/osaf/services/infrastructure/nid/config/opensafd.service \
        ${D}${systemd_unitdir}/system
    install -m 0644 ${B}/contrib/plmc/config/*.service ${D}/${systemd_unitdir}/system

    if [ ! -d "${D}${sysconfdir}/init.d" ]; then
        install -d ${D}${sysconfdir}/init.d
        install -m 0755 ${B}/osaf/services/infrastructure/nid/scripts/opensafd ${D}${sysconfdir}/init.d/
    fi
}

FILES_${PN} += "${localstatedir}/run ${systemd_unitdir}/system/*.service"
FILES_${PN}-staticdev += "${PKGLIBDIR}/*.a"

INSANE_SKIP_${PN} = "dev-so"

RDEPENDS_${PN} += "bash python"

