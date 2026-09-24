#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Compatibility entry point for the catalogue builder.

The old hand-curated definitions were removed because some entries contained
incorrect or mixed source references. The canonical catalogue is now rebuilt
only from pinned Sahih al-Bukhari and Sahih Muslim source records.
"""

from expand_sunnah_catalog import main

if __name__ == "__main__":
    main()
