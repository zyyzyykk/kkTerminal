import os
import re
import json

# i18n JSON
TRANSLATION_FILE_PATH = "../front/terminal/src/locales/en_US.json"

# search dir
SEARCH_DIR = "../front/terminal/src"
# ignore dirs
IGNORE_DIRS = ['assets', 'locales']

# To store all extracted keys for comparison later
extracted_keys = set()

def load_translations(file_path):
    """
    Load the i18n JSON file and return a collection of keys.
    :param file_path: i18n JSON file path
    :return: collection containing all i18n keys
    """
    try:
        with open(file_path, "r", encoding="utf-8") as f:
            translations = json.load(f)
            jsonKeySets = set(translations.keys())
            # Reset file pointer
            f.seek(0)
            # Count the number of lines
            line_count = sum(1 for _ in f)
            if len(jsonKeySets) != line_count - 2:
                print(f"Error: Duplicate keys at {file_path}.")
                exit(1)
        return jsonKeySets
    except FileNotFoundError:
        print(f"Error: Translation file not found at {file_path}.")
        exit(1)
    except json.JSONDecodeError as e:
        print(f"Error: Invalid JSON format in translation file. {e}")
        exit(1)

def get_all_files(directory, extensions, ignore_dirs=[]):
    """
    Retrieve all files in the specified directory and its subdirectories that match the extension, and support ignoring the specified folder.
    :param directory: Root directory to search for
    :param extensions: List of file extensions that need to be filtered
    :param ignore_dirs: List of folder names to ignore
    :return: List of file paths that need to be checked
    """
    matched_files = []
    for root, dirs, files in os.walk(directory):  # loop the directory
        # Filter folders to ignore
        dirs[:] = [d for d in dirs if d not in ignore_dirs]
        for file in files:
            if any(file.endswith(ext) for ext in extensions):
                matched_files.append(os.path.join(root, file))
    return matched_files

def process_file_line_by_line(file_path, translation_keys):
    """
    Process files by line, check for unpacked Chinese and keys that do not exist in the i18n JSON file.
    :param file_path: file path
    :param translation_keys: Key set in i18n JSON file
    """
    issues = []

    # Match $t(...) and i18n.global.t(...)
    wrapped_pattern = re.compile(r"(?:\$t|i18n\.global\.t|i18n\.global\.k)\(['\"]([^'\"]*?)['\"]\)")
    # Match Chinese
    chinese_pattern = re.compile(r"[\u4e00-\u9fff]")
    # Match comments
    comment_pattern = re.compile(r"(//.*?$|/\*.*?\*/|<!--.*?-->)", re.DOTALL | re.MULTILINE)

    with open(file_path, "r", encoding="utf-8") as f:
        for line_num, line in enumerate(f, start=1):
            # Remove comments
            line_no_comments = re.sub(comment_pattern, "", line)

            # Find all packed by $t() or i18n.global.t()
            wrapped_chinese_matches = wrapped_pattern.findall(line_no_comments)

            # Verify if packed contents exist in i18n JSON file
            for key in wrapped_chinese_matches:
                extracted_keys.add(key)  # Collect the key
                if "\\u00A0" in key:
                    continue
                if key not in translation_keys:
                    issues.append(f"{file_path}:{line_num} Not in i18n JSON: {key}")

            # Remove all contents packed by t() from the line
            line_no_wrapped = re.sub(wrapped_pattern, "", line_no_comments)

            # Check if there is any Chinese in the remaining content
            unwrapped_chinese = chinese_pattern.findall(line_no_wrapped)

            if unwrapped_chinese:
                issues.append(f"{file_path}:{line_num} Unprocessed Chinese: {''.join(unwrapped_chinese)}")

    return issues

def process_files():
    """
    Process files in the current directory and subdirectories, check for Chinese characters that have not been wrapped and keys that do not exist in the i18n JSON file.
    """
    extensions = [".js", ".vue"]
    files = get_all_files(SEARCH_DIR, extensions, IGNORE_DIRS)
    
    # Load i18n JSON file
    translation_keys = load_translations(TRANSLATION_FILE_PATH)
    
    all_issues = []

    for file in files:
        issues = process_file_line_by_line(file, translation_keys)
        all_issues.extend(issues)

    if len(extracted_keys) < len(translation_keys):
        all_issues.append(
            f"Key mismatch: {len(extracted_keys)} keys found in files, "
            f"but {len(translation_keys)} keys exist in the i18n JSON file.")
    
    if all_issues:
        print("Issues Found: " + str(len(all_issues)))
        for issue in all_issues:
            print(issue)
        exit(1)  # Exit with error code for CI/CD pipeline
    else:
        print("No Problem")  # Clear output for GitHub Actions success

if __name__ == "__main__":
    process_files()