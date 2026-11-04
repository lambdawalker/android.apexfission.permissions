import pathlib
import re
import unittest
import xml.etree.ElementTree as ET

ROOT = pathlib.Path(__file__).resolve().parents[2]
RES = ROOT / "permission/src/main/res"
LOCALES = ("de", "es", "fr", "it", "ja", "ko", "pl", "pt", "ru", "uk", "zh-rCN", "zh-rTW")


def strings(path):
    nodes = ET.parse(path).getroot().findall("string")
    names = [node.attrib["name"] for node in nodes]
    if len(names) != len(set(names)):
        raise AssertionError(f"Duplicate keys: {path}")
    return {node.attrib["name"]: "".join(node.itertext()) for node in nodes}


class PermissionLocalizationTest(unittest.TestCase):
    def test_all_languages_match_english_keys_and_placeholders(self):
        english = strings(RES / "values/strings.xml")
        for locale in LOCALES:
            with self.subTest(locale=locale):
                translated = strings(RES / f"values-{locale}/strings.xml")
                self.assertEqual(set(english), set(translated))
                for key, value in translated.items():
                    self.assertTrue(value.strip(), key)
                    self.assertEqual(
                        sorted(re.findall(r"%\\d+\\$[ds]", english[key])),
                        sorted(re.findall(r"%\\d+\\$[ds]", value)),
                        key,
                    )


if __name__ == "__main__":
    unittest.main()
