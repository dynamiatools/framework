/**
 * Shell tab-completion script generators for SFS CLI.
 *
 * Usage:
 *   # Bash  — add to ~/.bashrc
 *   eval "$(sfs completion bash)"
 *
 *   # Zsh   — add to ~/.zshrc  (compinit must already be loaded)
 *   eval "$(sfs completion zsh)"
 *
 *   # Fish  — save to completions dir
 *   sfs completion fish > ~/.config/fish/completions/sfs.fish
 *
 * Dynamic completion: bucket and identity names are resolved at completion
 * time by calling `sfs list buckets` / `sfs list identities`, so they always
 * reflect the current runtime state.
 */

export function bashCompletion(): string {
  return `
_sfs_buckets() {
  sfs list buckets 2>/dev/null | grep '"name"' | awk -F'"' '{print $4}'
}

_sfs_identities() {
  sfs list identities 2>/dev/null | grep '"identity"' | awk -F'"' '{print $4}'
}

_sfs_complete() {
  local cur prev words cword
  _init_completion 2>/dev/null || {
    COMPREPLY=()
    cur="\${COMP_WORDS[COMP_CWORD]}"
    prev="\${COMP_WORDS[COMP_CWORD-1]}"
    words=("\${COMP_WORDS[@]}")
    cword=\$COMP_CWORD
  }

  local top_commands="serve create list remove grant revoke upload copy provision completion"

  # Handle --data-dir path completion
  if [[ "\$prev" == "--data-dir" ]]; then
    COMPREPLY=( $(compgen -d -- "\$cur") )
    return
  fi

  case "\${words[1]}" in
    serve)
      COMPREPLY=( $(compgen -W "--host --port --data-dir --log-level" -- "\$cur") )
      ;;
    create)
      case "\$cword" in
        2) COMPREPLY=( $(compgen -W "bucket identity" -- "\$cur") ) ;;
      esac
      ;;
    list)
      case "\$cword" in
        2) COMPREPLY=( $(compgen -W "buckets identities files" -- "\$cur") ) ;;
        3)
          if [[ "\${words[2]}" == "files" ]]; then
            COMPREPLY=( $(compgen -W "$(_sfs_buckets)" -- "\$cur") )
          fi
          ;;
        4)
          # path inside bucket — fall back to filesystem
          if [[ "\${words[2]}" == "files" ]]; then
            COMPREPLY=( $(compgen -f -- "\$cur") )
          fi
          ;;
      esac
      ;;
    remove)
      case "\$cword" in
        2) COMPREPLY=( $(compgen -W "bucket identity" -- "\$cur") ) ;;
        3)
          if [[ "\${words[2]}" == "bucket" ]]; then
            COMPREPLY=( $(compgen -W "$(_sfs_buckets)" -- "\$cur") )
          elif [[ "\${words[2]}" == "identity" ]]; then
            COMPREPLY=( $(compgen -W "$(_sfs_identities)" -- "\$cur") )
          fi
          ;;
      esac
      ;;
    grant)
      case "\$cword" in
        2) COMPREPLY=( $(compgen -W "$(_sfs_identities)" -- "\$cur") ) ;;
        3) COMPREPLY=( $(compgen -W "$(_sfs_buckets)" -- "\$cur") ) ;;
        *) COMPREPLY=( $(compgen -W "--read --write --delete --prefix --data-dir" -- "\$cur") ) ;;
      esac
      ;;
    revoke)
      case "\$cword" in
        2) COMPREPLY=( $(compgen -W "$(_sfs_identities)" -- "\$cur") ) ;;
        3) COMPREPLY=( $(compgen -W "$(_sfs_buckets)" -- "\$cur") ) ;;
      esac
      ;;
    upload)
      case "\$cword" in
        2) COMPREPLY=( $(compgen -W "$(_sfs_buckets)" -- "\$cur") ) ;;
        4) COMPREPLY=( $(compgen -f -- "\$cur") ) ;;  # local file
        *) COMPREPLY=( $(compgen -W "--overwrite --data-dir" -- "\$cur") ) ;;
      esac
      ;;
    copy)
      case "\$cword" in
        2|4) COMPREPLY=( $(compgen -W "$(_sfs_buckets)" -- "\$cur") ) ;;
        *) COMPREPLY=( $(compgen -W "--overwrite --data-dir" -- "\$cur") ) ;;
      esac
      ;;
    provision)
      case "\$cword" in
        2) COMPREPLY=( $(compgen -W "$(_sfs_buckets)" -- "\$cur") ) ;;
        *) COMPREPLY=( $(compgen -W "--identity --prefix --read --write --delete --data-dir" -- "\$cur") ) ;;
      esac
      ;;
    completion)
      COMPREPLY=( $(compgen -W "bash zsh fish" -- "\$cur") )
      ;;
    *)
      COMPREPLY=( $(compgen -W "\$top_commands" -- "\$cur") )
      ;;
  esac
}

complete -F _sfs_complete sfs
complete -F _sfs_complete sfs-test
`.trim()
}

export function zshCompletion(): string {
  return `
#compdef sfs sfs-test

_sfs_buckets() {
  local -a buckets
  buckets=( \${(f)"$(sfs list buckets 2>/dev/null | grep '"name"' | awk -F'"' '{print $4}')"} )
  echo "\${buckets[@]}"
}

_sfs_identities() {
  local -a ids
  ids=( \${(f)"$(sfs list identities 2>/dev/null | grep '"identity"' | awk -F'"' '{print $4}')"} )
  echo "\${ids[@]}"
}

_sfs() {
  local state line
  typeset -A opt_args

  _arguments -C \\
    '1: :->command' \\
    '*:: :->args'

  case \$state in
    command)
      local commands
      commands=(
        'serve:Start the SFS HTTP server'
        'create:Create a bucket or identity'
        'list:List buckets, identities or files'
        'remove:Remove a bucket or identity'
        'grant:Grant permissions to an identity on a bucket'
        'revoke:Revoke permissions from an identity on a bucket'
        'upload:Upload a local file to a bucket'
        'copy:Copy a file between buckets'
        'provision:Auto-generate identity + secret and grant access to a bucket'
        'completion:Print shell completion script'
      )
      _describe 'command' commands
      ;;
    args)
      case \$line[1] in
        serve)
          _arguments \\
            '--host[Host to listen on]:host' \\
            '--port[Port to listen on]:port' \\
            '--data-dir[Data directory]:dir:_files -/' \\
            '--log-level[Log level]:level:(trace debug info warn error)'
          ;;
        create)
          case \$line[2] in
            bucket)
              _arguments \\
                '2:bucket name' \\
                '3:absolute path:_files -/'
              ;;
            identity)
              _arguments \\
                '2:identity name' \\
                '3:secret'
              ;;
            *)
              local subcmds; subcmds=('bucket:Create a bucket' 'identity:Create an identity')
              _describe 'subcommand' subcmds
              ;;
          esac
          ;;
        list)
          case \$line[2] in
            files)
              local buckets; buckets=( \$(_sfs_buckets) )
              _arguments \\
                '2:bucket:(\${buckets[@]})' \\
                '3:path:_files'
              ;;
            *)
              local subcmds; subcmds=('buckets:List all buckets' 'identities:List all identities' 'files:List files in a bucket')
              _describe 'subcommand' subcmds
              ;;
          esac
          ;;
        remove)
          case \$line[2] in
            bucket)
              local buckets; buckets=( \$(_sfs_buckets) )
              _arguments '3:bucket name:(\${buckets[@]})'
              ;;
            identity)
              local ids; ids=( \$(_sfs_identities) )
              _arguments '3:identity:(\${ids[@]})'
              ;;
            *)
              local subcmds; subcmds=('bucket:Remove a bucket' 'identity:Remove an identity')
              _describe 'subcommand' subcmds
              ;;
          esac
          ;;
        grant)
          local ids; ids=( \$(_sfs_identities) )
          local buckets; buckets=( \$(_sfs_buckets) )
          _arguments \\
            '2:identity:(\${ids[@]})' \\
            '3:bucket:(\${buckets[@]})' \\
            '--read[Grant read]' \\
            '--write[Grant write]' \\
            '--delete[Grant delete]' \\
            '--prefix[Path prefix]:prefix' \\
            '--data-dir[Data directory]:dir:_files -/'
          ;;
        revoke)
          local ids; ids=( \$(_sfs_identities) )
          local buckets; buckets=( \$(_sfs_buckets) )
          _arguments \\
            '2:identity:(\${ids[@]})' \\
            '3:bucket:(\${buckets[@]})' \\
            '--data-dir[Data directory]:dir:_files -/'
          ;;
        upload)
          local buckets; buckets=( \$(_sfs_buckets) )
          _arguments \\
            '2:bucket:(\${buckets[@]})' \\
            '3:key' \\
            '4:local file:_files' \\
            '--overwrite[Overwrite existing file]' \\
            '--data-dir[Data directory]:dir:_files -/'
          ;;
        copy)
          local buckets; buckets=( \$(_sfs_buckets) )
          _arguments \\
            '2:source bucket:(\${buckets[@]})' \\
            '3:source key' \\
            '4:destination bucket:(\${buckets[@]})' \\
            '5:destination key' \\
            '--overwrite[Overwrite existing file]' \\
            '--data-dir[Data directory]:dir:_files -/'
          ;;
        provision)
          local buckets; buckets=( \$(_sfs_buckets) )
          _arguments \\
            '2:bucket:(\${buckets[@]})' \\
            '--identity[Identity name]:name' \\
            '--prefix[Path prefix]:prefix' \\
            '--read[Grant read]' \\
            '--write[Grant write]' \\
            '--delete[Grant delete]' \\
            '--data-dir[Data directory]:dir:_files -/'
          ;;
        completion)
          _arguments '2:shell:(bash zsh fish)'
          ;;
      esac
      ;;
  esac
}

_sfs
`.trim()
}

export function fishCompletion(): string {
  return `
# Fish completion for sfs / sfs-test
# Install: sfs completion fish > ~/.config/fish/completions/sfs.fish

function __sfs_buckets
  sfs list buckets 2>/dev/null | string match -r '"name": "([^"]+)"' | string replace -r '.*"name": "([^"]+)".*' '$1'
end

function __sfs_identities
  sfs list identities 2>/dev/null | string match -r '"identity": "([^"]+)"' | string replace -r '.*"identity": "([^"]+)".*' '$1'
end

function __sfs_using_command
  set cmd (commandline -opc)
  if test (count $cmd) -eq 1
    return 0
  end
  return 1
end

function __sfs_using_subcommand
  set cmd (commandline -opc)
  if test (count $cmd) -ge 2; and test $cmd[2] = $argv[1]
    return 0
  end
  return 1
end

# Top-level commands
complete -c sfs -f -n '__sfs_using_command' -a serve       -d 'Start the SFS HTTP server'
complete -c sfs -f -n '__sfs_using_command' -a create      -d 'Create a bucket or identity'
complete -c sfs -f -n '__sfs_using_command' -a list        -d 'List resources'
complete -c sfs -f -n '__sfs_using_command' -a remove      -d 'Remove a resource'
complete -c sfs -f -n '__sfs_using_command' -a grant       -d 'Grant permissions'
complete -c sfs -f -n '__sfs_using_command' -a revoke      -d 'Revoke permissions'
complete -c sfs -f -n '__sfs_using_command' -a upload      -d 'Upload a local file'
complete -c sfs -f -n '__sfs_using_command' -a copy        -d 'Copy a file between buckets'
complete -c sfs -f -n '__sfs_using_command' -a provision   -d 'Auto-generate identity and grant access'
complete -c sfs -f -n '__sfs_using_command' -a completion  -d 'Print shell completion script'

# serve options
complete -c sfs -f -n '__sfs_using_subcommand serve' -l host       -d 'Host to listen on'
complete -c sfs -f -n '__sfs_using_subcommand serve' -l port       -d 'Port to listen on'
complete -c sfs -f -n '__sfs_using_subcommand serve' -l data-dir   -d 'Data directory'
complete -c sfs -f -n '__sfs_using_subcommand serve' -l log-level  -a 'trace debug info warn error' -d 'Log level'

# create subcommands
complete -c sfs -f -n '__sfs_using_subcommand create' -a bucket   -d 'Create a bucket'
complete -c sfs -f -n '__sfs_using_subcommand create' -a identity -d 'Create an identity'

# list subcommands
complete -c sfs -f -n '__sfs_using_subcommand list' -a buckets    -d 'List all buckets'
complete -c sfs -f -n '__sfs_using_subcommand list' -a identities -d 'List all identities'
complete -c sfs -f -n '__sfs_using_subcommand list' -a files      -d 'List files in a bucket'

# remove subcommands
complete -c sfs -f -n '__sfs_using_subcommand remove' -a bucket   -d 'Remove a bucket'
complete -c sfs -f -n '__sfs_using_subcommand remove' -a identity -d 'Remove an identity'

# grant / revoke options
complete -c sfs -f -n '__sfs_using_subcommand grant'  -l read    -d 'Grant read'
complete -c sfs -f -n '__sfs_using_subcommand grant'  -l write   -d 'Grant write'
complete -c sfs -f -n '__sfs_using_subcommand grant'  -l delete  -d 'Grant delete'
complete -c sfs -f -n '__sfs_using_subcommand grant'  -l prefix  -d 'Path prefix'

# provision options
complete -c sfs -f -n '__sfs_using_subcommand provision' -l identity -d 'Identity name'
complete -c sfs -f -n '__sfs_using_subcommand provision' -l prefix   -d 'Path prefix'
complete -c sfs -f -n '__sfs_using_subcommand provision' -l read     -d 'Grant read'
complete -c sfs -f -n '__sfs_using_subcommand provision' -l write    -d 'Grant write'
complete -c sfs -f -n '__sfs_using_subcommand provision' -l delete   -d 'Grant delete'

# completion shells
complete -c sfs -f -n '__sfs_using_subcommand completion' -a 'bash zsh fish'

# Dynamic bucket completions
complete -c sfs -f -n '__sfs_using_subcommand provision' -a '(__sfs_buckets)'
complete -c sfs -f -n '__sfs_using_subcommand upload'    -a '(__sfs_buckets)'
complete -c sfs -f -n '__sfs_using_subcommand copy'      -a '(__sfs_buckets)'

# Also apply to sfs-test alias
complete -c sfs-test --wraps sfs
`.trim()
}

