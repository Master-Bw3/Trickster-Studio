import {
  Sidebar,
  SidebarContent,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarRail,
  useSideBar,
} from "@/components/ui/sidebar"
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from "./ui/collapsible"
import { Setter } from "solid-js"

// Menu items.
const items = [
  {
    title: "Home",
    url: "#",
  },
  {
    title: "Inbox",
    url: "#",
  },
  {
    title: "Calendar",
    url: "#",
  },
  {
    title: "Search",
    url: "#",
  },
  {
    title: "Settings",
    url: "#",
  },
]

export function SpellSidebar(props: {setToggleSidebar: Setter<() => void>}) {
  const toggleSidebar = useSideBar().toggleSidebar
  {props.setToggleSidebar(() => toggleSidebar)}


  return (
    <Sidebar class="left-auto">
      <SidebarContent>
        <Collapsible >
          <CollapsibleTrigger class="bg-zinc-800 p-1 text-sm w-full text-left	">⌄ Spells</CollapsibleTrigger>
          <CollapsibleContent class="bg-background">
            Yes. Free to use for personal and commercial projects. No attribution
            required.
          </CollapsibleContent>

        </Collapsible>

        <Collapsible >
          <CollapsibleTrigger class="bg-zinc-800 p-1 text-sm w-full text-left	">⌄ Macros</CollapsibleTrigger>
          <CollapsibleContent class="bg-background">
            Yes. Free to use for personal and commercial projects. No attribution
            required.
          </CollapsibleContent>

        </Collapsible>
        <div class="bg-background h-full" style="margin-top: -0.5rem"></div>
      </SidebarContent>
    </Sidebar>
  )
}
