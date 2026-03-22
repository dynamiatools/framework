#!/usr/bin/env bash
# Dynamia Tools — Full Toolchain Bootstrap
# Usage: curl -fsSL https://get.dynamia.tools | bash
#
# Installs: git, curl, zip/unzip, JDK 25 (via SDKMAN), Node.js LTS (via fnm),
#           and the @dynamia-tools/cli npm package.
#
# Supported: Ubuntu/Debian, Fedora/RHEL/CentOS, Arch Linux, macOS (Homebrew)
# Not supported: Windows

set -euo pipefail

# ---------------------------------------------------------------------------
# ANSI colors (no external deps)
# ---------------------------------------------------------------------------
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
RESET='\033[0m'

info()    { echo -e "${CYAN}ℹ  $*${RESET}"; }
success() { echo -e "${GREEN}✓  $*${RESET}"; }
warn()    { echo -e "${YELLOW}⚠  $*${RESET}"; }
error()   { echo -e "${RED}✗  $*${RESET}" >&2; exit 1; }
banner()  {
  echo -e "${CYAN}${BOLD}"
  echo '  ____                              _       _____           _     '
  echo ' |  _ \ _   _ _ __   __ _ _ __ ___ (_) __ |_   _|__   ___ | |___ '
  echo ' | | | | | | | '"'"'_ \ / _` | '"'"'_ ` _ \| |/ _` || |/ _ \ / _ \| / __|'
  echo ' | |_| | |_| | | | | (_| | | | | | | | (_| || | (_) | (_) | \__ \'
  echo ' |____/ \__, |_| |_|\__,_|_| |_| |_|_|\__,_||_|\___/ \___/|_|___/'
  echo '         |___/                                                     '
  echo -e "${RESET}"
  echo -e "${BOLD}  Dynamia Tools — Toolchain Installer${RESET}"
  echo ""
}

# ---------------------------------------------------------------------------
# OS / package manager detection
# ---------------------------------------------------------------------------
OS=""
PM=""

detect_os() {
  if [[ "$OSTYPE" == "darwin"* ]]; then
    OS="macos"
    PM="brew"
  elif command -v apt-get &>/dev/null; then
    OS="debian"
    PM="apt-get"
  elif command -v dnf &>/dev/null; then
    OS="fedora"
    PM="dnf"
  elif command -v pacman &>/dev/null; then
    OS="arch"
    PM="pacman"
  else
    error "Unsupported OS. Please install dependencies manually and then run: npm install -g @dynamia-tools/cli"
  fi
  info "Detected OS: ${OS} (package manager: ${PM})"
}

# ---------------------------------------------------------------------------
# Install a single prerequisite package if missing
# ---------------------------------------------------------------------------
install_pkg() {
  local cmd="$1"
  local pkg="${2:-$1}"

  if command -v "$cmd" &>/dev/null; then
    success "$cmd already installed"
    return
  fi

  info "Installing $pkg..."
  case "$PM" in
    brew)     brew install "$pkg" ;;
    apt-get)  sudo apt-get install -y "$pkg" ;;
    dnf)      sudo dnf install -y "$pkg" ;;
    pacman)   sudo pacman -S --noconfirm "$pkg" ;;
  esac
}

# ---------------------------------------------------------------------------
# Install prerequisites
# ---------------------------------------------------------------------------
install_prerequisites() {
  info "Checking prerequisites..."

  install_pkg curl
  install_pkg git

  # git is REQUIRED — hard stop if unavailable
  if ! command -v git &>/dev/null; then
    error "git could not be installed. git is required for template cloning. Please install git manually and re-run this script."
  fi

  install_pkg zip
  install_pkg unzip

  success "Prerequisites ready"
}

# ---------------------------------------------------------------------------
# JDK 25 via SDKMAN
# ---------------------------------------------------------------------------
install_java() {
  info "Checking JDK 25..."

  if ! command -v sdk &>/dev/null; then
    info "Installing SDKMAN..."
    curl -s "https://get.sdkman.io" | bash
    # shellcheck source=/dev/null
    source "$HOME/.sdkman/bin/sdkman-init.sh"
  fi

  if java -version 2>&1 | grep -qE '^(openjdk|java) version "25'; then
    success "JDK 25 already installed"
  else
    info "Installing JDK 25 via SDKMAN..."
    sdk install java 25-tem
    sdk default java 25-tem
    success "JDK 25 installed"
  fi
}

# ---------------------------------------------------------------------------
# Node.js LTS via fnm
# ---------------------------------------------------------------------------
install_node() {
  info "Checking Node.js..."

  if ! command -v fnm &>/dev/null; then
    info "Installing fnm (Fast Node Manager)..."
    curl -fsSL https://fnm.vercel.app/install | bash
    export PATH="$HOME/.local/share/fnm:$PATH"
    eval "$(fnm env --use-on-cd)"
  fi

  if command -v node &>/dev/null; then
    success "Node.js already installed ($(node --version))"
  else
    info "Installing Node.js LTS via fnm..."
    fnm install --lts
    fnm use lts-latest
    fnm default lts-latest
    success "Node.js LTS installed"
  fi
}

# ---------------------------------------------------------------------------
# Patch shell profile (SDKMAN + fnm init lines)
# ---------------------------------------------------------------------------
patch_shell_profile() {
  local profile=""

  case "$SHELL" in
    */zsh)  profile="$HOME/.zshrc" ;;
    */bash) profile="$HOME/.bashrc" ;;
    *)      profile="$HOME/.profile" ;;
  esac

  info "Patching shell profile: $profile"

  # SDKMAN init
  local sdkman_line='source "$HOME/.sdkman/bin/sdkman-init.sh"'
  if ! grep -q "sdkman-init.sh" "$profile" 2>/dev/null; then
    echo "" >> "$profile"
    echo "# SDKMAN" >> "$profile"
    echo 'export SDKMAN_DIR="$HOME/.sdkman"' >> "$profile"
    echo "[[ -s \"\$HOME/.sdkman/bin/sdkman-init.sh\" ]] && $sdkman_line" >> "$profile"
    success "Added SDKMAN init to $profile"
  else
    success "SDKMAN already configured in $profile"
  fi

  # fnm init
  local fnm_line='eval "$(fnm env --use-on-cd)"'
  if ! grep -q "fnm env" "$profile" 2>/dev/null; then
    echo "" >> "$profile"
    echo "# fnm (Fast Node Manager)" >> "$profile"
    echo 'export PATH="$HOME/.local/share/fnm:$PATH"' >> "$profile"
    echo "$fnm_line" >> "$profile"
    success "Added fnm init to $profile"
  else
    success "fnm already configured in $profile"
  fi
}

# ---------------------------------------------------------------------------
# Install CLI and launch
# ---------------------------------------------------------------------------
install_cli() {
  info "Installing @dynamia-tools/cli..."
  npm install -g @dynamia-tools/cli
  success "@dynamia-tools/cli installed"
  echo ""
  info "Launching project wizard..."
  dynamia new
}

# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------
main() {
  banner
  detect_os
  install_prerequisites
  install_java
  install_node
  patch_shell_profile
  install_cli
}

main "$@"
